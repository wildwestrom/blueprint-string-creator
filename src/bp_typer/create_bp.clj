(ns bp-typer.create-bp
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as string])
  (:import (java.util HexFormat)))

(defn- bp-data-from-tile-array [tile-array {:keys [bp-label]}]
  {:blueprint
   {:item "blueprint"
    :icons [{:index 1, :signal {:name "signal-A", :type "virtual"}}
            {:index 2, :signal {:name "signal-B", :type "virtual"}}
            {:index 3, :signal {:name "signal-C", :type "virtual"}}
            {:index 4, :signal {:name "signal-D", :type "virtual"}}]
    :label bp-label
    :tiles (vec tile-array)
    :version 281479274299391}})

(defn- hex-to-pixel-vec-2d [hex-string width]
  (let [op (condp = width
             8 {:format-str "%8s"
                :partition 2}
             16 {:format-str "%16s"
                 :partition 4})]
    (mapv (fn [x]
            (mapv (fn [y]
                    (Integer/parseInt (str y)))
                  x))
          (map #(string/replace
                 (format (:format-str op)
                         (Integer/toBinaryString
                          (HexFormat/fromHexDigits
                           (string/join %))))
                 " " "0")
               (partition (:partition op) (seq hex-string))))))

(defn extract-coordinate [m]
  (let [coordinate (dissoc m :value)
        x (:x coordinate)
        y (:y coordinate)]
    [x y]))

(defn pixel-vec-2d-to-coordinates
  [vec-2d x-offset y-offset]
  (mapv extract-coordinate
        (filter #(= 1 (:value %))
                (flatten
                 (keep-indexed
                  (fn [index value]
                    (map #(assoc % :y (+ @y-offset index))
                         value))
                  (map (fn add-x-to-each-column [column]
                         (keep-indexed
                          (fn [index value]
                            {:x (+ index @x-offset) :value value})
                          column))
                       vec-2d))))))

(defn- coordinates-to-tiles [pixel-vec-2d tile-name]
  (mapv (fn [[x y]]
          {:name tile-name
           :position {:x x :y y}})
        pixel-vec-2d))

(defn- hex-to-tile-array [{:keys [hex-code width char]} tile-name x-offset y-offset tab-width]
  (condp = char
    \newline (do (reset! x-offset 0)
                 (swap! y-offset #(+ 17 %))
                 nil)
    \space (do (swap! x-offset #(+ 8 %))
               nil)
    \tab (do (swap! x-offset #(+ (* 8 (tab-width)) %))
             nil)
    (let [tiles (-> hex-code
                    (hex-to-pixel-vec-2d width)
                    (pixel-vec-2d-to-coordinates x-offset y-offset)
                    (coordinates-to-tiles tile-name))]
      (swap! x-offset #(+ % width))
      tiles)))

(def ^:private unifont-data
  (set (map (fn [s]
              ((fn [[idx val]]
                 {:id (HexFormat/fromHexDigits idx) :val val})
               (string/split s #":")))
            (string/split (slurp (io/resource "unifont_jp-14.0.03.hex"))
                          #"\n"))))

(defn- select-font-data-by-codepoint
  [cp]
  (set/select #(= cp (:id %)) unifont-data))

(def hex-lookup-errormsg
  "For some reason there were multiple matches on the same hex value. Very strange indeed.")

(defn- codepoint-to-hex [{:keys [cp char]}]
  (let [bits (:val ((fn [s]
                      (assert (= 1 (count s)) hex-lookup-errormsg)
                      (first s))
                    (select-font-data-by-codepoint cp)))]
    {:hex-code bits
     :char char
     :width (/ (count bits)
               4)}))

(defn- char-to-codepoint [char]
  (assert (= java.lang.Character (type char)))
  {:cp (int char)
   :char char})

(defn- string-to-codepoints [string]
  (map char-to-codepoint string))

(defn string-to-bp [opts]
  (let [{:keys [tile-name text tab-width]} opts]
    (bp-data-from-tile-array
     (apply concat
            (let [current-x-offset (atom 0)
                  current-y-offset (atom 0)]
              (map (fn [hex-val] (hex-to-tile-array hex-val
                                                    tile-name
                                                    current-x-offset
                                                    current-y-offset
                                                    tab-width))
                   (map codepoint-to-hex
                        (string-to-codepoints text)))))
     {:bp-label text})))

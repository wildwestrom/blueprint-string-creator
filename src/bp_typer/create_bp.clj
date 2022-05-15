(ns bp-typer.create-bp
  (:require [bp-typer.encoder :refer [bp-encode]]
            [jsonista.core :as j]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.set :as set])
  (:import (java.util HexFormat)))

(defn- create-array-of-tiles [vec-2d tile-name]
  (mapv (fn [[x y]]
          {:name tile-name
           :position {:x x :y y}})
        vec-2d))

(defn- bp-from-tile-array [tile-array]
  (bp-encode
   (j/write-value-as-string
    {:blueprint
     {:item "blueprint",
      :icons [{:index 1, :signal {:name "refined-concrete", :type "item"}}],
      :label "Blueprint",
      :tiles tile-array
      :version 281479274299391}})))

(defn- hex-to-2d-vec [hex-string width]
  (let [op (cond
             (= width 32) {:format-str "%8s"
                           :partition 2}
             (= width 64) {:format-str "%16s"
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

(defn- hex-to-tile-array [{:keys [hex-code width char-num]} tile-name]
  (create-array-of-tiles
   (mapv (fn [m]
           (let [coordinate (dissoc m :value)
                 x (:x coordinate)
                 y (:y coordinate)]
             [x y]))
         (filter #(= 1 (:value %))
                 (map (fn [m] (update m :x #(+ (* (/ width 4) char-num) %)))
                      (flatten
                       (keep-indexed
                        (fn [index value] (map #(assoc % :y index) value))
                        (map (fn [vec-entry] (keep-indexed
                                              (fn [index value] {:x index :value value}) vec-entry))
                             (hex-to-2d-vec hex-code width)))))))
   tile-name))

(def ^:private unifont-data
  (set (map (fn [s]
              ((fn [[idx val]]
                 {:id (HexFormat/fromHexDigits idx) :val val})
               (string/split s #":")))
            (string/split (slurp (io/resource "unifont_jp-14.0.03.hex"))
                          #"\n"))))

(defn- grab-data-by-codepoint [idx cp]
  (let [data (:val (#(when (= 1 (count %))
                       (first %))
                    (set/select #(= cp (:id %)) unifont-data)))
        idx (if idx idx 0)]
    {:hex-code data
     :char-num idx
     :width (count data)}))

(defn- list-of-hex-data [list-of-ints]
  (keep-indexed grab-data-by-codepoint list-of-ints))

(defn- char-to-int [char]
  (assert (= java.lang.Character (type char)))
  (int char))

(fn grab-data-by-codepoint [idx cp]
  (let [data (:val (#(when (= 1 (count %))
                       (first %))
                    (set/select #(= cp (:id %)) unifont-data)))
        idx (if idx idx 0)]
    {:hex-code data
     :char-num idx
     :width (count data)}))

(defn- string-to-ints [string]
  (map char-to-int string))

(defn string-to-bp [string & {:keys [tile-name]}]
  (when-not tile-name
    "refined-concrete")
  (bp-from-tile-array
   (apply concat
          (map
           #(hex-to-tile-array
             % tile-name)
           (keep-indexed
            grab-data-by-codepoint
            (string-to-ints string))))))

(ns bp-typer.core
  (:require [bp-typer.encoder :refer [bp-encode]]
            [jsonista.core :as j]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.set :as set])
  (:import (java.util HexFormat))
  (:gen-class))

(defn- create-array-of-tiles [list-of-coords tile-name]
  (mapv (fn [[x y]]
          {:name tile-name
           :position {:x x :y y}})
        list-of-coords))

(defn- bp-from-tile-array [tile-array]
  (bp-encode
   (j/write-value-as-string
    {:blueprint
     {:item "blueprint",
      :icons [{:index 1, :signal {:name "refined-concrete", :type "item"}}],
      :label "Blueprint",
      :tiles tile-array
      :version 281479274299391}})))

(defn- hex-to-2d-vec [hex-string]
  (let [length (count hex-string)
        op (cond
             (= length 32) {:format-str "%8s"
                            :partition 2}
             (= length 64) {:format-str "%16s"
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

(defn- hex-to-tile-array [hex-string]
  (mapv #(let [coordinate (dissoc % :value)
               x (:x coordinate)
               y (:y coordinate)]
           [x y])
        (filter #(= 1 (:value %))
                (flatten
                 (keep-indexed
                  (fn [index value] (map #(assoc % :y index) value))
                  (map (fn [vec-entry] (keep-indexed
                                        (fn [index value] {:x index :value value}) vec-entry))
                       (hex-to-2d-vec hex-string)))))))

(defn- hex-to-bp [hex-string]
  (bp-from-tile-array
   (create-array-of-tiles (hex-to-tile-array hex-string) "refined-concrete")))

(def ^:private unifont-data
  (set (map (fn [s]
          ((fn [[idx val]]
             {:id (HexFormat/fromHexDigits idx) :val val})
           (string/split s #":")))
        (string/split (slurp (io/resource "unifont_jp-14.0.03.hex")) #"\n"))))

(defn- grab-data-by-codepoint [cp]
  (:val (#(when (= 1 (count %))
       (first %))
    (set/select #(= cp (:id %)) unifont-data))))

(defn- char-to-int [char]
  (assert (= java.lang.Character (type char)))
  (int char))

(defn- char-to-bp [char]
  (hex-to-bp (grab-data-by-codepoint (char-to-int char))))

(defn -main [& args]
  (assert (= 1 (count args)))
  (println (char-to-bp (first (seq (first args))))))

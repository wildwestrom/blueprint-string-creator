(ns bp-typer.core
  (:require [bp-typer.decoder :refer [bp-decode]]
            [bp-typer.encoder :refer [bp-encode]]
            [jsonista.core :as j]))

(def test-string
  "0eJyVkk0KgzAQRu8y6wgmCtYse43SRdRpOxCjmLRUJHdv1NIK/XUVknnfG5jMAIU+Y9uRcSAHoLIxFuRuAEtHo/T45voWQQI5rIGBUfV46/BABqso8GWHDsEzIFPhFST3ewaONM6itrHkqDGjKlQjwaAPB/ffXC8xPqXidaF4bpU+U79b/Evf3ckq9590lEz4YkTWNQajVrnTW16s5Ge/WOn/wIffnnZDLlaJwQU7OxnEhqdZLrJU5HmSh0FoVWBYLNg+aO9vgybQqw==")

(j/read-value (bp-decode test-string)
              j/keyword-keys-object-mapper)

(defn create-array-of-tiles [list-of-coords tile-name]
  (mapv (fn [[x y]]
         {:name tile-name
          :position {:x x :y y}})
       list-of-coords))

(defn bp-from-tile-array [tile-array]
  (bp-encode
  (j/write-value-as-string
   {:blueprint
    {:item "blueprint",
     :icons [{:index 1, :signal {:name "refined-concrete", :type "item"}}],
     :label "Blueprint",
     :tiles tile-array
     :version 281479274299391}})))

(bp-from-tile-array
  (create-array-of-tiles [[1 2] [4 3]] "refined-concrete"))


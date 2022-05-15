(ns bp-typer.core
  (:gen-class)
  (:require [bp-typer.create-bp :refer [string-to-bp]]
            [bp-typer.encoder :refer [bp-encode]]
            [jsonista.core :as j]))

(defn- encode-bp-data [bp-data]
  (bp-encode (j/write-value-as-string bp-data)))

(defn -main [& args]
  (assert (= 1 (count args)))
  (println (encode-bp-data
            (string-to-bp (first args)
                          {:tile-name "stone-path"
                           :line-spacing 1}))))

#_(println (encode-bp-data (string-to-bp
                            "Test text\n+ a newline\n\twith a tab")))

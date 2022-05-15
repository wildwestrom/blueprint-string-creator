(ns bp-typer.core
  (:gen-class)
  (:require [bp-typer.create-bp :refer [string-to-bp]]))

(defn -main [& args]
  (assert (= 1 (count args)))
  (println (string-to-bp (first args) :tile-name "refined-concrete")))

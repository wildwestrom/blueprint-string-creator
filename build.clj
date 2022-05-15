(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'wildwestrom/bp-typer)
(def version (format "0.0.%s-alpha" (b/git-count-revs nil)))

(defn ci [opts]
  (-> opts
      (assoc :lib lib :version version :main 'bp-typer.core)
      (bb/clean)
      (bb/uber)))

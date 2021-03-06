(ns bp-typer.core
  (:require [bp-typer.encoder :refer [bp-encode]]
            [bp-typer.create-bp :refer [string-to-bp]]
            [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [jsonista.core :as j])
  (:gen-class))

(def cli-options
  [["-t" "--tile-name [TILENAME]" "Name of the tile you want to write with."
    :parse-fn #(if (empty? %) "stone-path" %)
    :default "stone-path"]
   ["-s" "--line-spacing [PIXELS]" "How many pixels of space between each line break."
    :parse-fn #(Integer/parseInt %)
    :default 1]
   [nil  "--tab-width [SPACES]" "Width of each tab by number of spaces."
    :parse-fn #(Integer/parseInt %)
    :default 4]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Convert any text into a blueprint string using GNU Unifont, a free pixel font."
        "Combine with a program that copies the text to your clipboard like `xclip` or `wl-copy`."
        ""
        "Usage: bp-string-creator [options] TEXT"
        ""
        "Options:"
        options-summary
        ""
        "Examples of tile names:"
        "stone-path"
        "concrete"
        "refined-concrete"]
       (string/join \newline)))

(defn- encode-bp-data [bp-data]
  (bp-encode (j/write-value-as-string bp-data)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with an error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)         {:exit-message (usage summary) :ok? true}
      errors                  {:exit-message (error-msg errors)}
      (= 1 (count arguments)) (assoc options :text (first arguments))
      (< 1 (count arguments)) {:exit-message (error-msg ["Only one text argument allowed."])}
      :else                   {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [exit-message ok?] :as options} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (println (encode-bp-data
                (string-to-bp options))))))

(comment
  (def cli-test '("Hello" "--tab-width" "2"))

  (-> cli-test
      validate-args
      string-to-bp
      encode-bp-data)

  (-> cli-test
      validate-args)

  (apply -main cli-test)

  (require '[bp-typer.decoder :refer [bp-to-map]])

  (bp-to-map "0eJxtkcsOwiAQRf9l1jSR2qSWpb9hXNB2qhORNjAaa8O/SzU+Il0R4J4z5DJBbS44OLIMagJqeutB7SbwdLDazGc8DggKiPEMAqw+zzvPvcWsdtScIAgg2+INlAx7AUwGX46h98TU29kSb7O1gDEuMvxpBs3HaEmBPAHi+xqHjItxmcSP+q5dm72pzGDHS+gqIR12ZPGLLlHpvDe1PDeW82xR/ZQu4IrOP535RhZllZdFXlXrKsqNrjF+AWw/6RAeVfWNGQ==")

  (bp-to-map "0eJx9z8sKwjAQheF3mXVc9AI12Xl5C3GR6iAD6bSkSVFK3t0Jgrgw3f5zvsWs0LuIkycOYFaggAOYn6aAbiPPYC5y5Ds+wVQKZnqwdRmwHVDAJ+wOsg+vKZeFfIiySUl9ZV2Wx23ZlOVpW7Zlef4nrwqc7VHmHJ2TATnM70tf0M80svyxr9pO111ba93oKqU3bshq4Q=="))

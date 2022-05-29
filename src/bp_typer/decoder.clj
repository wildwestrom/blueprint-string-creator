(ns bp-typer.decoder
  (:require [clojure.java.io :as io]
            [jsonista.core :as j])
  (:import (java.util Base64)
           (java.util.zip InflaterInputStream)))

(defn- remove-version-byte [bp-str]
  (->> bp-str
       .getBytes
       (drop 1)
       byte-array))

(defn- base64-decode [encoded-bytes]
  (-> (Base64/getDecoder)
      (.decode encoded-bytes)))

(defn- zlib-decode [zlib-compressed-string]
  (.readAllBytes
   (InflaterInputStream.
    (io/input-stream zlib-compressed-string))))

(defn bp-decode [bp-str]
  (-> bp-str
      remove-version-byte
      base64-decode
      zlib-decode
      String.))

(defn bp-to-map [bp-str]
  (-> bp-str
      bp-decode
      (j/read-value j/keyword-keys-object-mapper)))

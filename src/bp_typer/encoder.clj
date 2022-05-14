(ns bp-typer.encoder
  (:require [clojure.java.io :as io])
  (:import (java.util Base64)
           (java.util.zip DeflaterInputStream)))

(defn- zlib-encode [uncompressed-string]
  (.readAllBytes (DeflaterInputStream.
                  (io/input-stream (.getBytes uncompressed-string)))))

(defn- base64-encode [byte-array]
  (.encode (Base64/getEncoder) byte-array))

(defn- add-version-byte [base-64-encoded-bytes]
  (byte-array (cons 48 base-64-encoded-bytes)))

(defn bp-encode [json-bp-string]
  (-> json-bp-string
      zlib-encode
      base64-encode
      add-version-byte
      String.))

(ns bp-typer.decoder
  (:require [clojure.java.io :as io])
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

(comment
  (require '[jsonista.core :as j])
  (def test-bp-string "0eJxdjsEOgjAQRP9lzuUAkmB79DeMh4Ib3Vjahi5GQvrvUkyM8bSZndm3s6J3M8WJvcCs4CH4BHNekfjmrSs7WSLBgIVGKHg7FpUkeKr6iYcHsgL7K71g6nxREHb0YcSQWDj4QtncqlZYysh/mGjljnK6/zA/lRSeNKUd0RzrttNN1zZaH/SGcranrSBO33TOb/dbQ0c=")
  (-> test-bp-string
      bp-decode
      (j/read-value j/keyword-keys-object-mapper)))

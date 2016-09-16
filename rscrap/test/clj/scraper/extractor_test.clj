(ns extractor.extractor-test
  (use [rscrap.core])
  (require [net.cgrand.enlive-html :as html]
           [clojure.java.io :as io]
           [extractor.credit-type :as ct]
           [extractor.util :as c]
           [net.cgrand.tagsoup])
  (import [java.io StringReader]))


(comment

(-> (io/resource "credittype.html")
    (io/file )
    (slurp )
    (clojure.string/replace #"\n|\t" "")
    (StringReader.)
    (html/html-resource))




  (select-keys
    (extract-data "material.html") [:params])


  (select-keys
    (extract-data "firstcredittype.html") [:params])


  (select-keys
    (extract-data "credittype.html") [:params])


  (-> (html/select (html/html-resource "material.html") selector)
      (c/extract-data))


  (-> (html/select (html/html-resource "material.html") selector)

      (c/extract-data))


  )

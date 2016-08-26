(ns rscrap.extractor-test
  (use [rscrap.extractor])
  (require [net.cgrand.enlive-html :as html]
           [rscrap.extractor-credit-type :as ct]
           [rscrap.extractor-util :as c]
           [net.cgrand.tagsoup]))


(comment


  (select-keys
    (extract-data "material.html") [:params])


  (select-keys
    (extract-data "firstcredittype.html") [:params])


  (select-keys
    (extract-data "credittype.html") [:params])


  (-> (html/select (html/html-resource "material.html") selector)
      (c/extract-data-batch))


  (-> (html/select (html/html-resource "material.html") selector)

      (c/extract-data-batch))


  )

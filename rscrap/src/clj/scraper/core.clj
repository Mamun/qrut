(ns scraper.core
  (require [net.cgrand.enlive-html :as html]
           [clojure.walk :as w]
           [scraper.credit-type :as ct]
           [scraper.util :as c]
           [net.cgrand.tagsoup]))




;(keyword :A)





(def selector #{[:select] [:input]})
(def error-selector [:font.errormessage])


(defn get-form-url [node]
  (->> (html/select node [[:form (html/attr-has :method "post")]])
       (mapcat #(html/attr-values % :action))
       (first)))



(html/set-ns-parser! net.cgrand.tagsoup/parser)



(defn get-error [node]
  (->> (html/select node error-selector)
       (map :content)
       (map first)))


(defmulti do-scrap (fn [url _] url))


(defmethod do-scrap
  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [_ node]
  {:params       (ct/extract-credit-data node)
   :errormessage (get-error node)})


(defmethod do-scrap
  :default
  [_ node]
  {:params       (c/extract-data (html/select node selector))
   :errormessage (get-error node)})



(defn scrap-data [r]
  (let [node (html/html-resource r)
        form (get-form-url node)]
    (-> (do-scrap form node)
        (assoc :url form)
        (assoc :node node))))








(comment


  (-> (html/select (html/html-resource "address.html") selector)
      (c/extract-data))

  (-> (html/select (html/html-resource "material.html") selector)
      (c/extract-data))

  (-> (html/html-resource "credittype.html")
      (scrap-data)
      (:params))

  (-> (scrap-data "credittype.html")
      (select-keys [:params]))


  (-> (html/html-resource "material.html")
      (scrap-data)
      (select-keys [:params :url]))



  ;;find radio button



  (-> (html/html-resource "credittype.html")
      (html/select [:.pannelint [:tr]]))

  )
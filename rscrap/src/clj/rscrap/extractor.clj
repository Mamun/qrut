(ns rscrap.extractor
  (require [net.cgrand.enlive-html :as html]
           [rscrap.extractor-credit-type :as ct]
           [rscrap.extractor-util :as c]
           [net.cgrand.tagsoup]))


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


(defmulti do-extract (fn [url _] url))


(defmethod do-extract
  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [_ node]
  {:params       (ct/extract-credit-data node)
   :errormessage (get-error node)})


(defmethod do-extract
  :default
  [_ node]
  {:params       (c/extract-data-batch (html/select node selector))
   :errormessage (get-error node)})



(defn extract-data [r]
  (let [node (html/html-resource r)
        form (get-form-url node)]
    (-> (do-extract form node)
        (assoc :url form)
        (assoc :node node))))





(comment



  (-> (html/select (html/html-resource "material.html") selector)
      (c/extract-data-batch))

  (-> (html/html-resource "credittype.html")
      (extract-data)
      (:params))

  (-> (extract-data "credittype.html")
      (select-keys [:params]))


  (-> (html/html-resource "material.html")
      (extract-data)
      (select-keys [:params :url]))



  ;;find radio button



  (-> (html/html-resource "credittype.html")
      (html/select [:.pannelint [:tr]]))

  )
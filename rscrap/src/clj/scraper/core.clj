(ns scraper.core
  (require [net.cgrand.enlive-html :as html]
           [clojure.walk :as w]
           [scraper.credit-type :as ct]
           [scraper.util :as c]
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


(defmulti do-scrap (fn [url _] url))


(defmethod do-scrap
  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [_ node]
  {:form-params  (ct/extract-credit-data node)
   :errormessage (get-error node)})


(defmethod do-scrap
  :default
  [_ node]
  {:form-params  (c/extract-data (html/select node selector))
   :errormessage (get-error node)})



(defn scrap-data [r]
  (let [node (html/html-resource r)
        form (get-form-url node)]
    (-> (do-scrap form node)
        (assoc :url form)
        ;(assoc :node node)
        )))


(defmulti extract-content (fn [node-map] (:url node-map)))

(defmethod extract-content
  :default
  [{:keys [node]}]
  node)

(defmethod extract-content
  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [{:keys [node]}]
  (ct/extract-credit-data node))


(def select-material [[:select (html/attr= :name "Instance_theDossierConditions_theMaterialInfo$0_mCode")]])
(def sales-ma [[:select (html/attr= :name "Instance_theDossierConditions_theVendorInfo_mSalesmanId")]])

(defmethod extract-content
  "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"
  [{:keys [node]}]
  (hash-map
    :m-code
    (-> (html/select node select-material)
        (html/select [:option]))
    :sales-man
    (-> (html/select node sales-ma)
        (html/select [:option]))))


(defn as-node-map [r]
  (let [node (-> (html/html-resource r)
                 (html/select [:form])
                 (c/postwalk-remove-new-line))]
    (hash-map :node node
              :url (get-form-url node))))



(defn form-params [{:keys [node]}]
  (c/extract-data (html/select node selector)))





(comment


  (-> (html/select (html/html-resource "address.html") selector)
      (c/extract-data))

  (-> (html/select (html/html-resource "material.html") selector)
      (c/extract-data))

  (->
    (as-node-map "public/old/material_old.html")
    ;(form-params)
     (extract-content)

    )

  (-> (as-node-map "public/old/credittype_old.html")
      (extract-content)
      )

  (-> (as-node-map "public/old/material_old.html")
      (extract-content)
      )


  #_(-> (scrap-data "public/old/material_old.html")
      (extract-content)
      )



  ;;find radio button



  (-> (html/html-resource "credittype.html")
      (html/select [:.pannelint [:tr]]))

  )
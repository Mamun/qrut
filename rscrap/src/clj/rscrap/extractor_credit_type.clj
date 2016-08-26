(ns rscrap.extractor-credit-type
  (require [net.cgrand.enlive-html :as html]
           [net.cgrand.tagsoup]
           [rscrap.extractor-util :as r]))


(def credit-input-selector #{[:select]
                             [[:input (html/but #{(html/attr= :type "radio")
                                                  (html/attr= :type "image")
                                                  (html/attr= :type "checkbox")})]]})


(def credit-radio-selector [:tr [:input (html/attr= :type "radio")
                                 (html/attr= :name "CALCULATION_TABLE")]])



(defn select-credit-line [node v]
  (html/select node [:table.pannelint [:tr (html/has [[(html/attr= :type "radio")
                                                       (html/attr= :name "CALCULATION_TABLE")
                                                       (html/attr= :value v)
                                                       ]])]]))


(defn extract-card-line [node v]
  (let [p (-> (select-credit-line node v)
              (first)
              (html/select [[:td]]))]
    (->> (map html/text p)
         (remove #(or (clojure.string/starts-with? % "\n")
                      (clojure.string/starts-with? % "---")))
         (map clojure.string/trim-newline))))



(defn extract-vat-line [node v]
  (->> (html/select node [[:input (html/attr-contains :name v)]])
       (r/extract-data-batch)))



(defn extract-credit-line-data [node]
  (->> (map :attrs (html/select node credit-radio-selector))
       (map (fn [v]
              (let [w (extract-vat-line node (get v :value))]
                (if (empty? w)
                  (assoc v :content (extract-card-line node (get v :value)) :type :card)
                  (assoc v :content w)))))))



(defn extract-xcode [v]
  (if-not (nil? v)
    (clojure.string/join ","
                         (re-seq #"\d+"
                                 v))))


(defn format-line [m]
  (-> (select-keys m [:value :type :content])
      (assoc :xcode (extract-xcode (:onmouseover m)))))



(defn format-line-batch [coll]
  (let [g-coll (group-by :name coll)]
    (reduce (fn [acc [k w]]
              (assoc acc k (map format-line w))
              ) {} g-coll)))



(defn extract-credit-data [node]
  (let [credit-line (format-line-batch (extract-credit-line-data node))
        credit-params (r/extract-data-batch (html/select node credit-input-selector))]
    (merge credit-line credit-params)))




(comment


  (extract-credit-data (html/html-resource "credittype.html"))

  (extract-card-line (html/html-resource "credittype.html"))

  (extract-credit-line-data (html/html-resource "credittype.html"))

  (select-credit-line (html/html-resource "credittype.html") "3310")

  )
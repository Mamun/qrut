(ns scraper.credit-type
  (require [net.cgrand.enlive-html :as html]
           [net.cgrand.tagsoup]
           [clojure.walk :as w]
           [scraper.util :as r]))


(def credit-input-selector #{[:select]
                             [[:input (html/but #{(html/attr= :type "radio")
                                                  (html/attr= :type "image")
                                                  (html/attr= :type "checkbox")})]]})


(def credit-radio-selector [:tr [:input (html/attr= :type "radio")
                                 (html/attr= :name "CALCULATION_TABLE")]])



(defn select-credit-line-td [node v]
  (-> node
      (html/select [:table.pannelint [:tr (html/has [[(html/attr= :type "radio")
                                                      (html/attr= :name "CALCULATION_TABLE")
                                                      (html/attr= :value v)
                                                      ]])]])
      (html/select [:td])))


(defn select-credit-line* [node v]
  (concat (select-credit-line-td node v)
          (html/select node [[:input (html/attr-contains :name v)]])))


(defn select-credit-line [node]
  (->> (map :attrs (html/select node credit-radio-selector))
       (map (fn [v]
              (select-credit-line* node (get v :value))))))


;(defn extra)

(defn extract-credit-line-data [node]
  (w/postwalk (fn [w]
                (condp = (:tag w)
                  :input (:attrs w)
                  :span (first (:content w))
                  :td (let [v (first (:content w))]
                        (if (string? v)
                          {:value v}
                          v))
                  w)
                ) node))



(defn extract-credit-line [node]
  (-> node
      (r/postwalk-remove-new-line)
      (select-credit-line)
      (extract-credit-line-data)))

;(:tag "asdf")


#_(defn extract-credit-line-data [node]
    (->> (map :attrs (html/select node credit-radio-selector))
         (map (fn [v]
                (let [w (select-credit-line* node (get v :value))]
                  (if (empty? w)
                    ;  (assoc v :content (extract-card-line node (get v :value)) :type :card)
                    (assoc v :content w)))))))

#_(defn extract-xcode [v]
    (if-not (nil? v)
      (clojure.string/join ","
                           (re-seq #"\d+"
                                   v))))

#_(defn format-line [m]
    (-> (select-keys m [:value :type :content])
        (assoc :xcode (extract-xcode (:onmouseover m)))))


#_(defn format-line-batch [coll]
    (let [g-coll (group-by :name coll)]
      (reduce (fn [acc [k w]]
                (assoc acc k (map format-line w))
                ) {} g-coll)))



(defn extract-credit-data [node]
  (let [credit-line (extract-credit-line node)
        credit-params (r/extract-data (html/select node credit-input-selector))]
    (assoc credit-params :credit-line credit-line)))



(defn as-from-data [param]
  (reduce (fn [acc v]
            (condp = (:type v)
              "checkbox" (assoc acc (:name v) (:checked v))
              (assoc acc (or (:name v) "desc") (:value v)))
            ) {} param))



(defn creditline-from-data [param-m]
  (map as-from-data param-m))


(defn credit-line [coll credit-value]
  (-> (filter (fn [c]
                (if (= credit-value (get c "CALCULATION_TABLE"))
                  true false)
                ) coll)
      (first)))


(defn find-check-credittype [coll]
  (reduce (fn [acc [v]]
            (if (= (:checked v) "true")
              (reduced (:value v))
              acc)
            ) nil coll))


(defn get-default-credit-line [coll]
  (let [w (creditline-from-data coll)
        v (find-check-credittype coll) ]

    (credit-line w v)))


(comment


  ;(apply conj [1 2 3] [ 56] )

  (-> (html/html-resource "credittype.html")
      (extract-credit-data)
      (:credit-line)
      (get-default-credit-line)
      ;(creditline-from-data)
      ;(credit-line "2601")

      )


  (-> (html/html-resource "credittype.html")
      (r/postwalk-remove-new-line)
      (select-credit-line)
      ;      (extract-credit-line-data)

      )


  (-> (html/html-resource "credittype.html")
      (r/postwalk-remove-new-line)
      ;(select-credit-line "2017")
      (select-credit-line* "2601")
      ;(select-vat-line "2601")
      #_(extract-card-line)
      )



  ;(map :attrs (html/select (html/html-resource "credittype.html") credit-radio-selector))

  (extract-credit-data (html/html-resource "credittype.html"))

  (extract-card-line (html/html-resource "credittype.html") "1620")

  (extract-credit-line-data (html/html-resource "credittype.html"))

  (select-credit-line-td (r/postwalk-remove-new-line (html/html-resource "credittype.html")) "2601")

  )
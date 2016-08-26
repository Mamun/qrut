(ns rscrap.extractor-util
  (require [clojure.walk :as w]
           [net.cgrand.enlive-html :as html]))


(defn newline? [v]
  (if (and
        (string? v)
        (= "" (clojure.string/trim-newline (clojure.string/trim v))))
    true
    false))


(defn select-content [node-m]
  (->> (remove newline? (get-in node-m [:content]))
       (reduce (fn [acc v] (merge acc v)))))


(defn extract-data [node-m]
  (condp = (:tag node-m)
    :select (hash-map (get-in node-m [:attrs :name])
                      (select-content node-m)
                      )
    :option (hash-map (get-in node-m [:attrs :value])
                      (first (get-in node-m [:content])))
    :input (hash-map (get-in node-m [:attrs :name])
                     (get-in node-m [:attrs :value]))
    node-m))


(defn extract-data-batch [node]
  (->> (w/postwalk
         (fn [v]
           (if (map? v)
             (extract-data v)
             v)) node)
       (reduce (fn [acc v] (merge acc v)) {})))



(defn extract-data-batch-as-coll [node]
  (w/postwalk
    (fn [v]
      (if (map? v)
        (extract-data v)
        v)) node))


(comment

  (html/select (html/html-resource "credittype.html") #{[:input] [:select]})

  (extract-data-batch-as-coll
    (html/select (html/html-resource "credittype.html") #{[:input] [:select]})
    )


  (extract-data-batch
    (html/select (html/html-resource "credittype.html") #{[:select]}))

  )


(ns scraper.util
  (require [clojure.walk :as w]
           [net.cgrand.enlive-html :as html]))


(defn empty-space? [v]
  (if (and
        (string? v)
        (= "" (clojure.string/trim v)))
    true
    false))


(defn empty-content? [m]
  (cond (and (map? m)
             (nil? (:attrs m)))
        (every? empty-space? (:content m))
        (and (string? m)
             (empty-space? m))
        true
        :else
        false))


(defn postwalk-remove-empty-content [node]
  (w/postwalk
    (fn [m]
      (if (and (map? m)
               (contains? m :content))
        (update-in m [:content] #(remove empty-content? %))
        m)
      ) node))



(defn replace-new-line-with-empty [r]
  (-> r
      (clojure.string/replace #"\u00a0" "")
      (clojure.string/replace #"\n|\t" "")
      (clojure.string/trim)))


(defn postwalk-remove-new-line [node]
  (->> node
       (w/postwalk
         (fn [v]
           (if (string? v)
             (replace-new-line-with-empty v)
             v)))
       (postwalk-remove-empty-content)))







(comment


  (map clojure.string/trim (list "\n     \n"))

  (-> (html/html-resource "credittype.html")

      (postwalk-remove-new-line)
      (postwalk-remove-empty-content)
      #_(html/select [:tr.pannelext])
      ;   (remove-new-line)
      ;;(extract-card-line "1620")
      )


  (empty-content? "asdf")


  (-> {:tag     :tr,
       :attrs   {:valign "middle", :height "30", :id "calculation_row_card"},
       :content (list
                  {:tag :td, :attrs {:valign "center"}, :content (list "For debit card")}
                  {:tag :td, :attrs nil, :content (list " ")}
                  {:tag :td, :attrs {:align "right"}, :content (list "---")})}
      (postwalk-remove-new-line)
      (postwalk-remove-empty-content)
      )


  #_(->> (list
           {:tag :td, :attrs {:valign "center"}, :content (list "For debit card")}
           {:tag :td, :attrs nil, :content (list)}
           {:tag :td, :attrs {:align "right"}, :content (list "---")})
         (remove empty-content?))


  ;(map clojure.string/trim (list " "))

  (empty-content? {:tag :td, :attrs nil, :content (list " ")})

  )




(defn select-content [node-m]
  (->> (remove empty-space? (get-in node-m [:content]))
       (reduce (fn [acc v] (merge acc v)))))


(defn extract-node-data [node-m]
  (condp = (:tag node-m)
    :select (hash-map (get-in node-m [:attrs :name])
                      (select-content node-m))
    :option (hash-map (get-in node-m [:attrs :value])
                      (first (get-in node-m [:content])))
    :input (hash-map (get-in node-m [:attrs :name])
                     (get-in node-m [:attrs :value]))
    node-m))


(defn extract-data [node]
  (->> (w/postwalk
         (fn [v]
           (if (map? v)
             (extract-node-data v)
             v)) node)
       (reduce (fn [acc v] (merge acc v)) {})))



(defn extract-data-batch-as-coll [node]
  (w/postwalk
    (fn [v]
      (if (map? v)
        (extract-node-data v)
        v)) node))









(defn gen-transform [target]
  [[(cond
      (string? target) html/text-node
      :else html/any-node)]
   (fn [v]
     (println "--" v)
     (when (not= target v) v)

     )])


(defn remove-node [coll & nodes]
  (html/at* coll (map gen-transform nodes)))

#_(remove-node enlive-node
               {:tag :title, :attrs nil, :content ["Stack Overflow"]}
               "\n")


(comment


  ;(clojure.string/replace "Hello \n hello" #"\n" "" )

  (slurp "credittype.html")



  (html/select (html/html-resource "credittype.html") #{[:input] [:select]})

  (extract-data-batch-as-coll
    (html/select (html/html-resource "credittype.html") #{[:input] [:select]})
    )


  (extract-data
    (html/select (html/html-resource "credittype.html") #{[:select]}))

  )


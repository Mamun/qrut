(ns app.view.credittype
  (:require [net.cgrand.enlive-html :as html]
            [extractor.core :as e]
            [app.view.common :as c]
            [extractor.util :as eu]))



(def calculation-table [[:input (html/attr= :name "CALCULATION_TABLE")]])
(def calculation-table-label [[:label (html/has [[(html/attr= :type "radio")
                                                  (html/attr= :name "CALCULATION_TABLE")]])]])
(def in-instalment [[:input (html/attr= :name "instalment")]])
(def in-ins-count [[:input (html/attr= :name "instalmentsCount")]])
(def select-interest-rate [[:input (html/attr= :name "interestRate")]])
(def rsv [[:input (html/attr= :name "rsv")]])


(defn update-last [w new-val]
  (conj (into (empty w) (butlast w)) new-val))


(html/defsnippet credittype-line-card-snippet "public/credittype.html"
                 [:table#credittype-table :tbody :tr.credit-card-line]
                 [creditline]
                 [:tr] (html/clone-for
                         [[type-m name-m t inst-count-m inst-m interest-m rsv-m] creditline]
                         calculation-table (html/set-attr :value (:value type-m))
                         calculation-table (html/set-attr :checked (:checked type-m))
                         calculation-table-label #(update-in % [:content] update-last (:value name-m))))



(html/defsnippet credittype-line-snippet "public/credittype.html"
                 [:table#credittype-table :tbody :tr.credit-line]
                 [creditline]
                 [:tr] (html/clone-for
                         [[type-m name-m t inst-count-m inst-m interest-m rsv-m] creditline]
                         calculation-table (html/set-attr :value (:value type-m))
                         calculation-table (html/set-attr :checked (:checked type-m))
                         calculation-table-label #(update-in % [:content] update-last (:value name-m))
                         in-ins-count (html/set-attr :value (:value inst-count-m))
                         in-ins-count (html/set-attr :name (:name inst-count-m))
                         in-instalment (html/set-attr :value (:value inst-m))
                         in-instalment (html/set-attr :name (:name inst-m))
                         select-interest-rate (html/set-attr :value (:value interest-m))
                         select-interest-rate (html/set-attr :name (:name interest-m))
                         rsv (html/set-attr :name (:name rsv-m))))


(defn contain-text-field? [[_ v]]
  (if (= v "text")
    true
    false))


(defn is-card? [coll]
  (reduce (fn [acc v]
            (if (some contain-text-field? v)
              (reduced false)
              acc)
            ) true coll))


(defn get-credit-line [{:keys [credit-line]}]
  (let [{card true vat false} (group-by is-card? credit-line)]
    (concat (credittype-line-card-snippet card)
            (credittype-line-snippet vat))))


(html/defsnippet credittype-snippet "public/credittype.html"
                 [:div#credittype]
                 [d credit-line]
                 [:div html/any] (html/transform-content (html/replace-vars d))
                 [:table#credittype-table :tbody] (html/content credit-line))


(defn get-temp-data []
  (-> (e/extract-data "credittype.html")
      (get-in [:params])
      (eu/view-data)))


(defn view []
  (let [d (get-temp-data)
        credit-line (get-credit-line d)]
    (->> (credittype-snippet d credit-line)
         (c/index-template "Hello from credit type ")
         (apply str)
         (c/html-response))))




(comment


  (->> (get-temp-data)
       (get-credit-line)
       (html/emit*)
       (apply str))

  (let [[card & credit-line :as w] (->> (e/extract-data "credittype.html")
                                        (:params)
                                        (eu/view-data)
                                        (:credit-line))]
    (-> credit-line
        (credittype-line-snippet)
        #_(html/emit*)
        #_(apply str)))




  (credittype-line-card-snippet)


  (let [d (-> (e/extract-data "credittype.html")
              (:params)
              (eu/view-data))
        data [{:description      "Feste Rate"
               :instalmentsCount 36
               :instalment       "99,35"
               :interestRate     "9,9"
               :rsv              "RSV"
               }]
        ]

    (-> (credittype-snippet d)
        (html/select [:#credittype-table])
        (html/at [:tbody :tr] (html/clone-for [i (range 0 1)]
                                              [[:input (html/attr= :name "CALCULATION_TABLE")]] (html/set-attr :value i)
                                              ))
        (html/select [:tbody :tr])

        ))


  (view)


  (->
    (get-in
      (e/extract-data "credittype.html") [:params])
    (eu/view-format)
    ;(select-keys [:Instance_theDossierConditions_theMaterialInfo$0_mPrice] )

    )


  (let [p (get-in
            [:params])]
    (->> (eu/view-data p)
         (credittype-snippet)
         (html/emit*)
         (apply str)))

  )
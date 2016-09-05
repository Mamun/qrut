(ns app.handler.credittype-view
  (:require [net.cgrand.enlive-html :as html]))

(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


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


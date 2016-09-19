(ns app.view.credittype
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
                         calculation-table (html/do-> #(if (:checked type-m)
                                                        ((html/set-attr :checked  "checked" )%)
                                                        %)
                                                        )
                         calculation-table-label #(update-in % [:content] update-last (:value name-m))))



(html/defsnippet credittype-line-snippet "public/credittype.html"
                 [:table#credittype-table :tbody :tr.credit-line]
                 [creditline]
                 [:tr] (html/clone-for
                         [[type-m name-m t inst-count-m inst-m interest-m rsv-m] creditline]
                         calculation-table (html/set-attr :value (:value type-m))
                         calculation-table (html/do-> #(if (:checked type-m)
                                                        ((html/set-attr :checked  :val )%)
                                                        %)
                                                      )
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


(def model-m {:Instance_theDossierConditions_theMaterialInfo$0_mPrice ""
               :Instance_theDossierConditions_theClassicInfo_mInsuranceAmount ""
               :CAM_prePaid ""
               :CAM_mCreditAmount ""
               :Instance_theDossierConditions_theMaterialInfo$0_mDeliveringDate ""
               })


(html/defsnippet credittype-snippet "public/credittype.html"
                 [:div#credittype]
                 [d credit-line]
                 [:div html/any] (html/transform-content (html/replace-vars (merge model-m d)))
                 [:table#credittype-table :tbody] (html/content credit-line))

(comment


  (->  {:RequestID "578106005",
        :Instance_theDossierConditions_theClassicInfo_mPaymentDelay "0",
        :CAM_prePaid "",
        :CAM_mCreditAmount "",
        :Instance_theDossierConditions_theClassicInfo_mInsuranceAmount "",
        :focus_helper "",
        :Instance_theDossierConditions_mPaymentDay {:15 "15.", :1 "1."},
        :Instance_theDossierConditions_mCreditTypeCode "2960",
        :Instance_theDossierConditions_thePartnerAssuranceInfo_mOptionCode "",
        :credit-line (),
        :Instance_theDossierConditions_mPaymentMode "P",
        :CAM_Instance_theDossierConditions_mCreditTypeCode {:2638 "Feste Rate unter 1000 EUR",
                                                            :3313 "Schlu�rate",
                                                            :0 "Bitte w�hlen",
                                                            :3311 "Aktionskauf"},
        :jump_to "",
        :UNIQUE_TRANSACTION "FLOWTID:2",
        :radio_last_selection "0",
        :Instance_theDossierConditions_theMaterialInfo$0_mDeliveringDate ""
        :Instance_theDossierConditions_theMaterialInfo$0_mPrice ""}
       (credittype-snippet nil)

       )

 ; (html/content nil)
  )
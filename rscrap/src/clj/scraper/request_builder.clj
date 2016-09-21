(ns scraper.request-builder)


(defn assoc-action-type [params submit-params ]
  ;(println "asssoc-action -ype ")
  (cond
    (contains? params :prev)
    (-> submit-params
        (dissoc  "prev" "next" "alternate3" "alternate1")
        (assoc  "prev.x" 35 "prev.y" 35))
    (contains? params :next)
    (-> submit-params
        (dissoc  "prev" "next" "alternate3" "alternate1")
        (assoc  "next.x" 14 "next.y" 14))
    :else
    (dissoc submit-params "next" "next.x" "next.y" "prev" "prev.x" "prev.y" "alternate3" "alternate1")))



(defmulti assoc-default-params (fn [url p]
                                 url
                                 ))


(defmethod assoc-default-params
  :default
  [_ r] r)

(defmethod assoc-default-params

  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [_ params]
  (assoc params
    :CAM_Instance_theDossierConditions_mCreditTypeCode "0"
    :Instance_theDossierConditions_mCreditTypeCode (or (get params "CALCULATION_TABLE")
                                                       (get params :CALCULATION_TABLE))
    :CAM_mCreditAmount (get params "Instance_theDossierConditions_theMaterialInfo$0_mPrice")))





(def default-customer-info {"accountTypeSelected"    "SEPA"
                            "account_type"           "SEPA"
                            "SCHUFA_AGREEMENT_CHECK" "1"})


(defmethod assoc-default-params
  "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentity"
  [_ params ]
  (merge params default-customer-info))






;(update-in {:form-params {nil nil} } [:form-params] dissoc nil )

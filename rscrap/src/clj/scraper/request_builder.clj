(ns scraper.request-builder
  (:require [scraper.core :as scraper]
            [clojure.walk :as w]))


(defn assoc-action-type [request-m user-params-m]
  (cond
    (contains? user-params-m :prev)
    (-> request-m
        (update-in [:form-params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
        (update-in [:form-params] (fn [w] (assoc w "prev.x" 35 "prev.y" 35))))

    (contains? user-params-m :next)
    (-> request-m
        (update-in [:form-params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
        (update-in [:form-params] (fn [w] (assoc w "next.x" 14 "next.y" 14))))
    :else
    (update-in request-m [:form-params] (fn [w] (dissoc w "next" "next.x" "next.y" "prev" "prev.x" "prev.y" "alternate3" "alternate1")))))


(defmulti format-request (fn [request-m _] (get request-m :url)))


(defmethod format-request
  :default
  [request-m user-params-m]
  (-> request-m
      (update-in [:form-params] (fn [_] (merge (scraper/form-params request-m) user-params-m)))
      (update-in  [:form-params] dissoc :credit-line)))


(defmethod format-request
  "/ratanet/front?controller=CreditApplication&action=Login"
  [request-m user-params-m]
  (-> request-m
      (assoc :form-params user-params-m)
      (assoc :cookie (clj-http.cookies/cookie-store))))


(defmethod format-request
  "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1"
  [request-m _]
  (dissoc request-m :form-params))



(defmulti assoc-default-params (fn [url p]
                                 url
                                 ))


(defmethod assoc-default-params
  :default
  [_ r] r)

(defmethod assoc-default-params
  "DispoPlusCreditType"
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
  "DispoV2CustomerIdentity"
  [_ params ]
  (merge params default-customer-info))



(defn merge-request [request-m {:keys [params]}]
  (-> request-m
      (format-request (w/stringify-keys (assoc-default-params (:action params ) params)))
      (assoc-action-type params)))


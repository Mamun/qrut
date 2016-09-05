(ns extractor.http-connector
  (require [clj-http.client :as client]
           ;[net.cgrand.tagsoup]
           [extractor.core :as p ])
  (import [java.io StringReader]))


(def cs (clj-http.cookies/cookie-store))




(defn get-page
  [{:keys [params url]}]
  (-> url
      (client/get {:cookie-store cs
                   :query-params params})
      (:body)
      (StringReader.)
      (p/extract-data)))



(defn submit-page [{:keys [params url debug?]}]
  (when debug?
    (println "--Submit data start for --" url)
    (clojure.pprint/pprint params)
    (println "--Submit data end--"))
  (-> url
      (client/post {:form-params     params
                    :cookie-store    cs
                    :force-redirects true})
      (:body)
      (StringReader.)
      (p/extract-data)))



(defn login-page [config-m]
  {:params (get config-m "/ratanet/front?controller=CreditApplication&action=Login")
   :url    (str (get config-m "url") "/ratanet/front?controller=CreditApplication&action=Login")})


(defn material-page [config-m]
  {:params {}
   :url    (str (get config-m "url") "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1")})


(defn next-page [m]
  (-> m
      (update-in [:params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
      (update-in [:params] (fn [w] (assoc w "next.x" 14 "next.y" 14)))))


(defn previous-page [m]
  (-> m
      (update-in [:params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
      (update-in [:params] (fn [w] (assoc w "prev.x" 35 "prev.y" 35)))))


(defn current-page [m]
  (update-in m [:params] (fn [w] (dissoc w "next" "next.x" "next.y" "prev" "prev.x" "prev.y" "alternate3" "alternate1"))))


(defn assoc-user-params [m config-m]
  (-> (merge-with merge m {:params (get config-m (:url m))})
      (update-in [:url] (fn [v]
                          (do
                            (str (get config-m "url") v))))
      (next-page)))



(defn credit-type-first-page [config-m]
  (assoc config-m "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
                  {"CAM_Instance_theDossierConditions_mCreditTypeCode"      0
                   "Instance_theDossierConditions_mPaymentDay"              1
                   "Instance_theDossierConditions_theMaterialInfo$0_mTaken" "E"
                   "Instance_theDossierConditions_theMaterialInfo$0_mPrice" 500
                   "CAM_mCreditAmount"                                      500}))




(defn create-contract [conf-m stop-url]
  (submit-page (login-page conf-m))
  (let [v (get-page (material-page conf-m))]
    (loop [state-m v]
      (cond (or (not-empty (:errormessage state-m))

                (= stop-url (:url state-m)))
            state-m
            (nil? (:url state-m))
            (throw (ex-info "action is not found " state-m))
            :else
            (if (= "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
                   (:url state-m))
              (-> (assoc-user-params state-m (credit-type-first-page conf-m))
                  (submit-page)
                  (current-page)
                  (assoc-user-params conf-m)
                  (submit-page)
                  (recur))
              (-> (assoc-user-params state-m conf-m)
                  (submit-page)
                  (recur)))))))




(comment

  #_(->> [[:form (html/attr-has :method "post")]]
       (html/select (html/html-resource "calc.html"))
       (mapcat #(html/attr-values % :action)
               ))




  )



;action=DispoV2CustomerIdentityComplementary

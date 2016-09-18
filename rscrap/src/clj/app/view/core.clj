(ns app.view.core
  (:require [clojure.walk :as w]
            [app.view.credittype :as ct]
            [app.view.material :as mt]
            [net.cgrand.enlive-html :as html]))


(defn- replace-mv
  [f1 m]
  (let [f (fn [[k v]] [(keyword k) (f1 v)])]
    (into {} (map f m))))


(defn postwalk-replace-value-with
  "Recursively transforms all map keys from strings to keywords."
  {:added "1.1"}
  [f m]
  (w/postwalk (fn [x] (cond
                        (map? x)
                        (replace-mv f x)
                        :else x)) m))


(defn view-data [m]
  (->> (w/keywordize-keys m)
    (postwalk-replace-value-with (fn [v]
                                   (if (nil? v) "" v)
                                   ) )))



(defn html-response
  [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})


(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


(html/deftemplate index-template "public/template.html"
                  [title content]
                  [:head :title] (html/content title)
                  [:div#wrapper] (html/content content))


(html/deftemplate login-template "public/login.html"
                  [title]
                  [:head :title] (html/content title))


(defmulti view (fn [request-m] (:url request-m)))


(defmethod view
  "/ratanet/front?controller=CreditApplication&action=Login"
  [_]
  (->> (login-template "Login ")
       (apply str)
       (html-response)))


(defmethod view
  "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"
  [request-m]
  (let [d (get-in request-m [:form-params "Instance_theDossierConditions_theMaterialInfo$0_mCode"])
        s (get-in request-m [:form-params "Instance_theDossierConditions_theVendorInfo_mSalesmanId"])]
    (->> (mt/material-snippet d s)
         ;(apply-session submit-m)
         (index-template "Select material  ")
         (apply str)
         (html-response))))


(defmethod view
  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [request-m]
  (let [d (get-in request-m [:form-params])
        credit-line (ct/get-credit-line d)
        d (view-data d)]
    (->> (ct/credittype-snippet d credit-line)
         (index-template "Select credit type ")
         (apply str)
         (html-response))))






(comment

  (apply str
         (login-template "Hello"))

  )
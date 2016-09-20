(ns app.view.core
  (:require [clojure.walk :as w]
            [app.view.credittype :as ct]
            [app.view.material :as mt]
            [scraper.core :as scraper]
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
                                      ))))



(defn html-response
  [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})


(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


(html/deftemplate index-template "public/template.html"
                  [title error content]
                  [:head :title] (html/content title)
                  [:div#wrapper] (html/content content)
                  [:div.alert.alert-danger] #(when (not-empty error)
                                              (html/at %
                                                       [html/any] (html/content (apply str error)))))


(html/deftemplate login-template "public/login.html"
                  [title]
                  [:head :title] (html/content title))


(html/defsnippet customer-snippet "public/customer.html"
                 [:div#customer]
                 []
                 identity)


(html/defsnippet customer-comple-snippet "public/customer_compl.html"
                 [:div#customer-compl]
                 []
                 identity)


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
  (let [r (scraper/extract-content request-m)]
    (->> (mt/material-snippet r)
         ;(apply-session submit-m)
         (index-template "Select material  " (scraper/get-error request-m))
         (apply str)
         (html-response))))


(defmethod view
  "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
  [request-m]
  (let [d (scraper/extract-content request-m)
        credit-line (ct/get-credit-line d)
        d (view-data d)]
    (->> (ct/credittype-snippet d credit-line)
         (index-template "Select credit type " (scraper/get-error request-m))
         (apply str)
         (html-response))))

(defmethod view
  "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentity"
  [request-m]
  (->> (customer-snippet)
       (index-template "Select credit type " (scraper/get-error request-m))
       (apply str)
       (html-response)
       )

  )



(defmethod view
  "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentityComplementary"
  [request-m]
  (->> (customer-comple-snippet)
       (index-template "Select credit type " (:errormessage request-m))
       (apply str)
       (html-response)
       )

  )






(comment

  (apply str
         (login-template "Hello"))

  )
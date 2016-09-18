(ns scraper.remote-fetcher
  (:require [clj-http.client :as client]
            [scraper.core :as p]
            [clojure.tools.reader.edn :as edn])
  (import [java.io StringReader]))


(def config (edn/read-string (slurp "credit_type_default.edn")))

(defn get-base-url []
  (get config "url"))


;(get-base-url)


(defn send-http-get
  [{:keys [url query-params cookie]}]
  (-> (str (get-base-url) url)
      (client/get {:cookie-store cookie
                   :query-params query-params})))



(defn send-http-post [{:keys [url form-params cookie]}]
  (-> (str (get-base-url) url)
      (client/post {:form-params     form-params
                    :cookie-store    cookie
                    :force-redirects true})))



(defmulti format-request (fn [request-m _] (get request-m :url)))


(defmethod format-request
  :default
  [request-m user-params-m]
  (update-in request-m [:form-params] merge (get user-params-m (:url request-m))))


(defmethod format-request
  "/ratanet/front?controller=CreditApplication&action=Login"
  [request-m user-params-m]
  (-> request-m
      (assoc :form-params (get user-params-m (:url request-m)))
      (assoc :cookie (clj-http.cookies/cookie-store))))


(defmethod format-request
  "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1"
  [request-m _]
  (dissoc request-m :form-params))


(defn assoc-action-type [request-m action-type]
  (condp = action-type
    :prev
    (-> request-m
        (update-in [:form-params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
        (update-in [:form-params] (fn [w] (assoc w "prev.x" 35 "prev.y" 35))))
    :current
    (update-in request-m [:form-params] (fn [w] (dissoc w "next" "next.x" "next.y" "prev" "prev.x" "prev.y" "alternate3" "alternate1")))
    (-> request-m
        (update-in [:form-params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
        (update-in [:form-params] (fn [w] (assoc w "next.x" 14 "next.y" 14))))))


(defn format-response [response {:keys [cookie]}]
  ; (clojure.pprint/pprint response)
  (-> response
      (:body)
      (StringReader.)
      (p/scrap-data)
      (assoc :cookie cookie)
      )
  )

(defn log [r]
  (println "------Log Start---------")
  (clojure.pprint/pprint r)
  (println "------Log End ---------")
  r
  )


(defn fetch-data
  ([request-m ] (fetch-data request-m :next))
  ([request-m  action-type]
   (log request-m)
   (if (contains? request-m :form-params)
     (-> request-m
         (assoc-action-type action-type)
         (send-http-post)

         (format-response request-m)
         #_(log))
     (-> (send-http-get request-m)
         (format-response request-m)))
   #_(let [request-m (format-request request-m user-params)]
    )))



(defn init-flow-request [request]
  (-> request
      (assoc :url "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1")
      (dissoc :form-params)))


(defn login-request []
  {:url "/ratanet/front?controller=CreditApplication&action=Login"})



(defn create-contract [user-params stop-url]
  (let [v (-> (login-request)
              (format-request user-params)
              (fetch-data)
              (init-flow-request))]
    (loop [request-m v]
      (cond (or (not-empty (:errormessage request-m))
                (= stop-url (:url request-m)))
            request-m
            ;  (nil? (:url user-params))
            ; request
            :else
            (recur (fetch-data (format-request request-m user-params)))))))



(comment

  (->> "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
       (create-contract config))

  (get config "/ratanet/front?controller=CreditApplication&action=DispoMaterialType")

  (-> (login-request)
      (fetch-data config)
      ;(init-flow-request)
     ; (send-request config)
      ;  (send-request config :prev)
      ;  (send-request config)
      )

  )


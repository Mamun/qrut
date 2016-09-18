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
      (update-in  [:form-params] merge user-params-m)
      (update-in  [:form-params] dissoc :credit-line)
      ))


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





(defn format-response [response {:keys [cookie]}]
  ; (clojure.pprint/pprint response)
  (-> response
      (:body)
      (StringReader.)
      (p/scrap-data)
      (assoc :cookie cookie)
      )
  )

(defn log
  ([r] (log r ""))
  ([r note]

   (println (str "------Log Start for ---------" note))
   (clojure.pprint/pprint r)
   (println "------Log End ---------")
   r))



(defn fetch-data
  [request-m]

  (if (contains? request-m :form-params)
    (-> request-m
        (send-http-post)
        (format-response request-m))
    (-> (send-http-get request-m)
        (format-response request-m)))
  )



(defn init-flow-request [request]
  (-> request
      (assoc :url "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1")
      (dissoc :form-params)))


(defn login-request []
  {:url "/ratanet/front?controller=CreditApplication&action=Login"})



(defn create-contract [user-params stop-url]
  (let [v (-> (login-request)
              (format-request user-params)
              (assoc-action-type user-params)
              (fetch-data)
              (init-flow-request))]
    (loop [request-m v]
      (cond (or (not-empty (:errormessage request-m))
                (= stop-url (:url request-m)))
            request-m
            ;  (nil? (:url user-params))
            ; request
            :else
            (-> (format-request request-m user-params)
                (assoc-action-type user-params)
                (fetch-data)
                (recur))))))



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


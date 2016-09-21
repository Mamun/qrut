(ns scraper.remote-fetcher
  (:require [clj-http.client :as client]
            [scraper.core :as p]
            [clojure.tools.reader.edn :as edn]
            [scraper.core :as scraper]
            [clojure.walk :as w]
            [scraper.request-builder :as rb])
  (import [java.io StringReader]))


(defn log
  ([note] (log {} note))
  ([r note]

   (println (str "------Log Start for ---------" note))
   (clojure.pprint/pprint (dissoc r :node))
   (println "------Log End ---------")
   r))



(def config (edn/read-string (slurp "credit_type_default.edn")))

(defn get-base-url []
  (get config "url"))



(defonce session-store (atom {}))

(comment
  (->
    (get @session-store 1)

    (scraper/get-error)
    )
  )


;@session-store

(defn add-to-store! [request-m identifier]
  (do
    (swap! session-store (fn [w]
                           (update-in w [identifier] (fn [_] request-m))))
    request-m))



;(get-in ring-request [:session :identifier])

(defn get-request-m [identifier]
  (get @session-store identifier))


(defmulti fetch-data (fn [request-m] (if (contains? request-m :form-params)
                                       :form-params)))

(defmethod fetch-data
  :default
  [{:keys [url query-params cookie]}]
  (-> (str (get-base-url) url)
      (client/get {:cookie-store cookie
                   :query-params query-params})
      (:body)
      (StringReader.)
      (p/as-node-map)
      (assoc :cookie cookie)))



(defmethod fetch-data
  :form-params
  [{:keys [url form-params cookie]}]
  ;(println  "Post Method is called -----##################")

  (-> (str (get-base-url) url)
      (client/post {:form-params     form-params
                    :cookie-store    cookie
                    :force-redirects true})
      (:body)
      (StringReader.)
      (p/as-node-map)
      (assoc :cookie cookie)))





(defn prepare-params [request-m params]
;  (println "------------------Prepare params -----------")
  (if (contains? request-m :query-params)
    (dissoc request-m :form-params)
    (let [form-params (scraper/form-params request-m)
          user-params (->> params
                           (rb/assoc-default-params (:url request-m))
                           (w/stringify-keys )
                           (merge form-params )
                           (rb/assoc-action-type params ))]
      ;(log user-params "Submit params ")
      (assoc request-m :form-params user-params))))

#_(defn assoc-form)
;(merge nil {:a 3})


(defn fetch-remote
  ([identifier params] (fetch-remote identifier params nil))
  ([identifier params request-m]
   (-> (get-request-m identifier)
       (merge request-m)
       (prepare-params params)
       (fetch-data)
       (add-to-store! identifier))))



(defn init-flow-request []
  {:url          "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1"
   :query-params {}})


(defn init-login-request []
  {:url    "/ratanet/front?controller=CreditApplication&action=Login"
   :cookie (clj-http.cookies/cookie-store)})



(defn fetch-remote-data [user-params request-m]
  (fetch-remote 100 (get user-params (:url request-m)) request-m))



(defn create-contract [user-params stop-url]
  (let [_ (fetch-remote-data user-params (init-login-request))
        w (fetch-remote-data user-params (init-flow-request))]
    (loop [request-m w]
      (cond (or (not-empty (scraper/get-error request-m))
                (= stop-url (:url request-m)))
            request-m
            :else
            (->
                (fetch-remote-data user-params request-m)
                (recur))))))


(defn assoc-next [config]
  (->>
    (map (fn [[k v]]
           (if (map? v)
             {k (assoc v :next 1)}
             {k v} ))
         config)
    (into {})
    ))


(comment



  (merge-with merge {:a {:a 3}} {:next 1} )


  (->> "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
       (create-contract (assoc-next config)))

  (get config "/ratanet/front?controller=CreditApplication&action=DispoMaterialType")




  (->> (assoc-next config)
       (create-contract "/ratanet/front?controller=CreditApplication&action=PrintingContract")

       )

  )


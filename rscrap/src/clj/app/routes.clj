(ns app.routes
  (:use [compojure.route :as route]
        [compojure.core])
  (:require [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.session.cookie :as sc]
            [ring.middleware.dadysql :as hs]
            [clojure.tools.logging :as log]
            [immutant.web.middleware :as imm]
            [dadysql.http-service :as h]
            [app.view :as v]
            [app.service :as api]
            [app.state :as s]))


(defn select-header [request]
  (select-keys request [:character-encoding :params :request-method
                        :content-length :uri :query-params :query-string
                        :context :remote-addr :path-info :server-name :session :server-port
                        :form-params :scheme :headers]))


(defn debug-request-params [r]
  (do
    (println "-----------")
    (clojure.pprint/pprint (:session r))
    (clojure.pprint/pprint (:params r))
    (println "End -----------")
    )
  )


(defn update-session-data [response {:keys [session params]} type]
  (assoc response :session (assoc session type params)))


(defn material-handler [r]
  (do
    (debug-request-params r)
    (->
      (v/credittype-view)
      (update-session-data r :material))))


(defn credittype-handler [r]
  (debug-request-params r)
  (if (get-in r [:params :next])
    (v/customer-view)
    (v/material-view (get-in r [:session :material]))))


(defn customer-handler [r]
  (if (get-in r [:params :next])
    (v/customer-comple-view)
    (v/credittype-view)))


(defn customer-comp-handler [r]
  (if (get-in r [:params :next])
    (v/index)
    (v/customer-view)))


(defroutes
  view-routes
  (GET "/" [_]
    (v/index))
  (GET "/index" _ (v/index))
  (GET "/material" r (v/material-view (get-in r [:session :material])))
  (POST "/material" r (material-handler r))
  (POST "/credittype" r (credittype-handler r))
  (POST "/customer" r (customer-handler r))
  (POST "/customerComplementary" r (customer-comp-handler r)))


(defn api-routes []
  (-> (routes
        (GET "/postcode" request (h/ok-response (select-header request)))
        (GET "/material" _ (h/response (api/load-material-type))))
      (h/warp-default)))


(defroutes
  app-routes
  view-routes
  (context "/api" _ (api-routes))
  (route/resources "/")
  (route/not-found {:status 200
                    :body   "Not found From app "}))


(defn warp-log [handler]
  (fn [req]
    (log/info "-----------------" req)
    (handler req)))


(def http-handler
  (-> app-routes
      (hs/warp-dadysql-handler :tms s/tms-atom :ds s/ds-atom)
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)
                         (assoc-in [:session :store] (sc/cookie-store {:key "BuD3KgdAXhDHrJXu"}))
                         (assoc-in [:session :cookie-name] "example-app-sessions")))
      (warp-log)
      (wrap-webjars)
      (imm/wrap-session)
      ;  wrap-with-logger
      ;wrap-gzip
      ))

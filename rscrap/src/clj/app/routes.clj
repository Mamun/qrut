(ns app.routes
  (:use [compojure.route :as route]
        [compojure.core])
  (:require [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.session.cookie :as sc]
            [ring.util.response :as response]
            [ring.middleware.dadysql :as hs]
            [clojure.tools.logging :as log]
            [immutant.web.middleware :as imm]
            [dadysql.http-service :as h]
            [app.routes-middleware :as rm]
            [app.handler.common :as common]
            [app.handler.credittype :as credit]
            [app.handler.material :as material]
            [app.service :as api]
            [app.state :as s]))


(defn select-header [request]
  (select-keys request [:character-encoding :params :request-method
                        :content-length :uri :query-params :query-string
                        :context :remote-addr :path-info :server-name :session :server-port
                        :form-params :scheme :headers]))


(defn material-handler [r]
  (do
    (response/redirect "/credittype")))


(defn credittype-handler [r]
  (do
    (response/redirect "/customer")))


(defn customer-handler [r]
  (do
    (response/redirect "/customerComplementary")))


(defn customer-comp-handler [r]
  (response/redirect "/"))



(defroutes
  view-routes
  (GET "/" [_]
    (response/redirect "/material"))
  (GET "/index" _ (common/index))

  (GET "/material" r  (material/view (get-in r [:session :action-v "/material"])))
  (POST "/material" r (material-handler r))

  (GET "/credittype" r (do

                         (credit/view (get-in r [:session :action-v "/material"]))

                           ))
  (POST "/credittype" r (do
                          ;(println "---------Credittype form data ")
                          ;  (clojure.pprint/pprint (:params r))
                          (credittype-handler r)))

  (GET "/customer" r (common/customer-view))
  (POST "/customer" r (customer-handler r))

  (GET "/customerComplementary" r (common/customer-comple-view))
  (POST "/customerComplementary" r (customer-comp-handler r)))


(defn api-routes []
  (-> (routes
        (GET "/postcode" request (h/ok-response (select-header request)))
        (GET "/material" _ (h/response (api/load-material-type))))
      (h/warp-default)))


(defroutes
  app-routes
  (rm/warp-navi-middleware view-routes "/material")
  (context "/api" _ (api-routes))
  (route/resources "/")
  (route/not-found {:status 200
                    :body   "Not found From app "}))


(defn warp-log [handler]
  (fn [req]
    (log/info "-----------------" (dissoc req :cookies :headers :async-channel :body))
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

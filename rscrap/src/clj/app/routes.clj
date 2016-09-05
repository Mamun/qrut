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
            [app.handler.common :as v]
            [app.handler.credittype :as vc]
            [app.handler.material :as mv]
            [app.service :as api]
            [app.state :as s]))


(defn select-header [request]
  (select-keys request [:character-encoding :params :request-method
                        :content-length :uri :query-params :query-string
                        :context :remote-addr :path-info :server-name :session :server-port
                        :form-params :scheme :headers]))


(defn debug-request-params [r]
  (do
    (println "-- Session start --")
    (clojure.pprint/pprint (:session r))
    (println "-- Session end --")
    (println "-- params start  --")
    (clojure.pprint/pprint (:params r))
    (println "End -----------")))


(defn build-page-flow [r]
  (if (get-in r [:params :prev])
    (update-in r [:session :flow] (fn [v]
                                    (into (empty v) (butlast v))))
    (update-in r [:session :flow] (fn [v]
                                    (conj (or v []) (get r :uri))))))


#_(defn update-session-data [response {:keys [session params]} type]
    (assoc response :session (assoc session type params)))


(defn init-material-handler [r]
  (-> (mv/view (get-in r [:session :material]))
      (update-in [:session] (fn [v] (assoc v :flow ["/material"])))

      ))


(defn material-handler [r]
  (do
    ;(debug-request-params r)
    (-> (vc/view)
        #_(update-session-data r :material))))


(defn credittype-handler [r]
  ;(debug-request-params r)
  (if (get-in r [:params :next])
    (v/customer-view)
    (mv/view (get-in r [:session :material]))))


(defn customer-handler [r]
  (if (get-in r [:params :next])
    (v/customer-comple-view)
    (vc/view)))


(defn customer-comp-handler [r]
  (if (get-in r [:params :next])
    (response/redirect "/")
    (v/customer-view)))


(defn dispatch-handler [path r]
  (condp = path
    "/material" (init-material-handler r)
    "/credittype" (credittype-handler r)
    "/customer" (customer-handler r)
    "/customerComplementary" (customer-comp-handler r)
    (init-material-handler r)))




(defroutes
  view-routes
  (GET "/" [_]
    (response/redirect "/material"))
  (GET "/index" _ (v/index))
  (GET "/material" r (init-material-handler r))
  (POST "/material" r (material-handler r))
  (POST "/credittype" r (credittype-handler r))
  (POST "/customer" r (customer-handler r))
  (POST "/customerComplementary" r (customer-comp-handler r)))


(defn warp-view-routes-middleware [handler]
  (fn [request]
    (debug-request-params request)
    (let [{:keys [session]} (build-page-flow request)]
      (-> request
          (handler)
          (update-in [:session] #(merge session (or % {})))))))



(defn api-routes []
  (-> (routes
        (GET "/postcode" request (h/ok-response (select-header request)))
        (GET "/material" _ (h/response (api/load-material-type))))
      (h/warp-default)))


(defroutes
  app-routes
  (warp-view-routes-middleware view-routes)
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

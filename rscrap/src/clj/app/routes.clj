(ns app.routes
  (:use [compojure.route :as route]
        [compojure.core])
  (:require [clojure.walk :as w]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.session.cookie :as sc]
            [ring.util.response :as response]
            [ring.middleware.dadysql :as hs]
            [clojure.tools.logging :as log]
            [immutant.web.middleware :as imm]
            [dadysql.http-service :as h]
            [app.routes-middleware :as rm]
    ;  [app.handler.common :as common]
    ;  [app.handler.credittype :as credit]
    ;  [app.handler.material :as material]
            [app.view.core :as view]
            [scraper.remote-fetcher :as fetcher]
            [app.service :as api]
            [app.state :as s]))


(defn select-header [request]
  (select-keys request [:character-encoding :params :request-method
                        :content-length :uri :query-params :query-string
                        :context :remote-addr :path-info :server-name :session :server-port
                        :form-params :scheme :headers]))


#_(defn material-handler [r]
    (do
      (response/redirect "/credittype")))


(defn credittype-handler [r]
  (do
    (response/redirect "/customer")))


(defn customer-handler [r]
  (do
    (response/redirect "/customerComplementary")))


(defn customer-comp-handler [r]
  (response/redirect "/material"))


(def redirect-url-m
  {"/login"                                                                 "/ratanet/front?controller=CreditApplication&action=Login"
   "/ratanet/front?controller=CreditApplication&action=Login"               "/login"
   "/material"                                                              "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"
   "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"   "/material"
   "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType" "/credittype"})


(defn find-redirect-utl [request-m r]

  (cond (empty? (:errormessage request-m))
        (or (get redirect-url-m (:url request-m))
            "/material"
            )

        :else
        (:uri r)))


(defonce session-store (atom {}))

(comment
  (->
    (get @session-store 1)

    (view/view-data)
    )
  )


;@session-store

(defn add-to-store! [request-m ring-request]
  (let [identifier (get-in ring-request [:session :identifier])]
    (swap! session-store (fn [w]
                           (update-in w [identifier] (fn [_] request-m))))
    request-m))


(defn get-request-m [ring-request]
  (get @session-store (get-in ring-request [:session :identifier])))


(defn copy-session [new-request old-request]
  (assoc new-request :session (:session old-request))
  )

(defn process-request [{:keys [uri params] :as rrequest}]
  (let [user-params-m (hash-map (get redirect-url-m uri) (w/stringify-keys params))]
    (-> (get-request-m rrequest)
        (fetcher/format-request user-params-m)
        (fetcher/log)
        (fetcher/fetch-data)
        (add-to-store! rrequest)
        (find-redirect-utl rrequest)

        (response/redirect)
        (copy-session rrequest))))



(defn assoc-idententifer-to-session [old-request]
  (let [identifier (or (get-in old-request [:session :identifier])
                       1)
        new-session (-> (or (:session old-request) {})
                        (assoc :identifier identifier))]
    (assoc old-request :session new-session)))



(defroutes
  auth-routes
  (GET "/login" rrequest (let [rrequest (assoc-idententifer-to-session rrequest)]
                           (-> (fetcher/login-request)
                               (add-to-store! rrequest)
                               (view/view)
                               (copy-session rrequest))))
  (POST "/login" rrequest (process-request rrequest))
  (GET "/logout" _ (response/redirect "/login")))


(defroutes
  credit-routes
  (GET "/" [_]
    (response/redirect "/login"))

  (GET "/material" rrequest (let []
                              ;(println "----/material .........." v)
                              (-> (get-request-m rrequest)
                                  (fetcher/init-flow-request)
                                  (fetcher/fetch-data)
                                  (add-to-store! rrequest)
                                  ;(sender/log)
                                  (view/view)
                                  (copy-session rrequest)))  #_(material/view (get-in r [:session :action-v "/material"])))
  (POST "/material" r (process-request r))

  (GET "/credittype" rrequest (do
                         (println "credot type voew ")
                         (-> (get-request-m rrequest)
                             (fetcher/log)
                             (view/view))
                         ))

  #_(POST "/credittype" r (do
                            (credittype-handler r)))

  ;(GET "/customer" r (common/customer-view))
  ;(POST "/customer" r (customer-handler r))

  ;(GET "/customerComplementary" r (common/customer-comple-view))
  ;(POST "/customerComplementary" r (customer-comp-handler r))

  )


(def api-routes
  (context "/api" _
    (GET "/session" request (h/response request))
    (GET "/postcode" request (h/ok-response (select-header request)))
    (GET "/material" _ (h/response (api/load-material-type)))))



(def app-routes
  (routes
    auth-routes
    (wrap-routes #'credit-routes rm/warp-navi-middleware "/material")
    (wrap-routes #'api-routes h/warp-default)
    (route/resources "/")
    (route/not-found {:status 200
                      :body   "Not found From app "})))



(defn warp-log [handler]
  (fn [req]
    (log/info "-----------------" (dissoc req :cookies :headers :async-channel :body :server-exchange))
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





(comment

  )
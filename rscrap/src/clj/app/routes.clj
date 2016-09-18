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
            [app.handler.core :as hc]
            [scraper.sender :as sender]
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
  ;  (clojure.pprint/pprint params)
  (let [user-params-m (hash-map (get redirect-url-m uri) (w/stringify-keys params))]
    (-> (get-request-m rrequest)
        (sender/format-request user-params-m)
        (sender/send-request)
        (add-to-store! rrequest)
        (find-redirect-utl rrequest)
        (sender/log)
        (response/redirect)

        (copy-session rrequest)
        #_(assoc :session (assoc session :identifier identifer)))))



#_(defn login-handler [request]
    (let [{:keys [params]} request

          user-r {"/ratanet/front?controller=CreditApplication&action=Login" params}
          request-m (-> (sender/login-request)
                        (sender/send-request user-r))]
      (if (empty? (:errormessage request-m))
        (let [request (update-in request [:session :identifer] (fn [v] 1))
              ;identifer 1
              ]
          ;(sender/init-flow-request request-m)
          (add-to-store! request-m 1)
          #_(process-request request)

          )
        (hc/view (sender/login-request)))))


(defn assoc-idententifer-to-session [old-request]
  (let [identifier (or (get-in old-request [:session :identifier])
                       1)
        new-session (-> (or (:session old-request) {})
                        (assoc :identifier identifier))]
    (assoc old-request :session new-session)))



(defroutes
  auth-routes
  (GET "/login" rrequest (let [rrequest (assoc-idententifer-to-session rrequest)]
                           (-> (sender/login-request)
                               (add-to-store! rrequest)
                               (hc/view)
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
                                  (sender/init-flow-request)
                                  (sender/send-request)
                                  (add-to-store! rrequest)
                                  (sender/log)
                                  (hc/view)
                                  (copy-session rrequest)))  #_(material/view (get-in r [:session :action-v "/material"])))
  (POST "/material" r (process-request r))

  (GET "/credittype" r (do
                         (println "credot type voew ")
                         (sender/log (get @session-store (get-in r [:session :identifier])))
                         (hc/view (get @session-store (get-in r [:session :identifier])))
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
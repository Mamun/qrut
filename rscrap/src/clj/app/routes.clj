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
        ;    [app.routes-middleware :as rm]
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
  {"/login"                                                                     "/ratanet/front?controller=CreditApplication&action=Login"
   "/ratanet/front?controller=CreditApplication&action=Login"                   "/login"
   "/material"                                                                  "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"
   "/credittype"                                                                "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"
   "/customer"                                                                  "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentity"
   "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"       "/material"
   "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"     "/credittype"
   "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentity" "/customer"})

(defn is-same-page [params]
  (if (or (contains? params :next)
          (contains? params :prev))
    false
    true))

(comment
  (is-same-page {"Instance_theDossierConditions_theMaterialInfo$0_mCode"  "320",
                 :Instance_theDossierConditions_theVendorInfo_mSalesmanId "2103257",
                 :next                                                    ""}
                )

  )



(defn find-redirect-utl [request-m {:keys [uri params]}]
  ; (fetcher/log params "Params value ")
  (cond
    #_(is-same-page params)
    ;uri
    (not-empty (:errormessage request-m))
    uri
    (empty? (:errormessage request-m))
    (do
      ;(fetcher/log request-m  "Redirect url ")
      (or (get redirect-url-m (:url request-m))
          "/material"))
    :else
    uri))


(defonce session-store (atom {}))

(comment
  (->
    (get @session-store 1)

    (view/view)
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
  (assoc new-request :session (:session old-request)))


(defn process-request [{:keys [uri params] :as rrequest}]
  (println "-------------------proc")
  (let [user-params-m (hash-map (get redirect-url-m uri) (w/stringify-keys params))]
    (fetcher/log user-params-m "proecess request start ")
    (-> (get-request-m rrequest)
        (fetcher/format-request user-params-m)
        (fetcher/assoc-action-type params)
        (fetcher/log "Before fetach ")
        (fetcher/fetch-data)
        (fetcher/log "After fetch  ")
        (add-to-store! rrequest)
        (find-redirect-utl rrequest)
        (fetcher/log "Redirect URL   ")
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


(defn credittype-default [{:keys [params] :as r}]

  (assoc r :params (assoc params :CAM_mCreditAmount (get params "Instance_theDossierConditions_theMaterialInfo$0_mPrice")))
  )

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
                                    (view/view))))
  (POST "/credittype" r (do
                          (println "---- credit type ")
                          (->
                            (credittype-default r)
                            (process-request))))

  (GET "/customer" r (do
                       (-> (get-request-m r)
                           (view/view))))
  (POST "/customer" r (process-request r))

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
    credit-routes
    ;(wrap-routes #'credit-routes rm/warp-navi-middleware "/material")
    (wrap-routes #'api-routes h/warp-default)
    (route/resources "/")
    (route/not-found {:status 200
                      :body   "Not found From app "})))



(defn warp-log [handler]
  (fn [req]
    (log/info "-----------------" (dissoc req :cookies :headers :async-channel :body :server-exchange))
    (handler req)
    ))



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
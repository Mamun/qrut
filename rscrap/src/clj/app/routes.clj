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
            [scraper.request-builder :as rb]
            [app.service :as api]
            [app.state :as s]))


(defn select-header [request]
  (select-keys request [:character-encoding :params :request-method
                        :content-length :uri :query-params :query-string
                        :context :remote-addr :path-info :server-name :session :server-port
                        :form-params :scheme :headers]))



(def redirect-url-m
  {"/ratanet/front?controller=CreditApplication&action=Login"                                "/login"
   "/ratanet/front?controller=CreditApplication&action=DispoMaterialType"                    "/credit?action=DispoMaterialType"
   "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType"                  "/credit?action=DispoPlusCreditType"
   "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentity"              "/credit?DispoV2CustomerIdentity"
   "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentityComplementary" "/credit?DispoV2CustomerIdentityComplementary"

   })



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
  (get redirect-url-m (:url request-m)))



(defn copy-session [new-request old-request]
  (assoc new-request :session (:session old-request)))


(defn post-request [rrequest]
  (-> (fetcher/get-request-m rrequest)
      (rb/merge-request rrequest)
      ;(fetcher/log "After merge request  ")
      (fetcher/fetch-data)
      (fetcher/add-to-store! rrequest)
      (find-redirect-utl rrequest)
      ;(fetcher/log "Redirect uzrl name ")
      (response/redirect)
      (copy-session rrequest)))




(defn assoc-idententifer-to-session [old-request]
  (let [identifier (or (get-in old-request [:session :identifier])
                       1)
        new-session (-> (or (:session old-request) {})
                        (assoc :identifier identifier))]
    (assoc old-request :session new-session)))



(defn login-handler [rrequest]
  (do
    (-> (fetcher/login-request)
        (rb/merge-request rrequest)
        (fetcher/fetch-data)
        (fetcher/add-to-store! rrequest))
    (->
      (response/redirect "/credit?action=DispoMaterialType")
      (copy-session rrequest))))


(defroutes
  auth-routes
  (GET "/login" rrequest (let [rrequest (assoc-idententifer-to-session rrequest)]
                           (-> (view/login-view)
                               (copy-session rrequest))))
  (POST "/login" rrequest (login-handler rrequest))
  (GET "/logout" _ (response/redirect "/login")))



(defn get-request [rrequest]

  (println "---- Get request ")

  (let [action (get-in rrequest [:params :action])]
    (if (= action "DispoMaterialType")
      (-> (fetcher/get-request-m rrequest)
          (fetcher/init-flow-request)
          (fetcher/fetch-data)
          (fetcher/add-to-store! rrequest)
          (view/view)
          (copy-session rrequest))

      (-> (fetcher/get-request-m rrequest)
          (view/view)))))



(defroutes
  credit-routes
  (GET "/" [_]
    (response/redirect "/login"))
  (GET "/credit" rrequest (get-request rrequest))
  (POST "/credit" rrequest (post-request rrequest)))



(def api-routes
  (context "/api" _
    (GET "/session" request (h/response request))
    (GET "/postcode" request (h/ok-response (select-header request)))
    (GET "/material" _ (h/response (api/load-material-type)))))



(def app-routes
  (routes
    #'auth-routes
    #'credit-routes
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
(ns app.routes-middleware
  (:require [ring.util.response :as response]))


(defn debug-request-params [r]
  (do
    (println "-- Session start --")
    (clojure.pprint/pprint (:session r))
    (println "-- Session end --")
    (println "-- params start  --")
    (clojure.pprint/pprint (:params r))
    (println "End -----------")))


(defn get-action [r]
  (get-in r [:session :action]))


(defn remove-last-action [r]
  (update-in r [:action] (fn [v]
                           (into (empty v) (butlast v)))))


(defn add-last-action [session r]
  (update-in session [:action] (fn [v]
                                 (conj (or v []) (get r :uri)))))



(defn update-session [response {:keys [session uri params] :as r}]
  (update-in response [:session] #(-> session
                                      (merge (or % {}))
                                      (assoc-in [:action-v uri] params)
                                      (add-last-action r))))


(defn init-session [response {:keys [session uri params]}]
  (update-in response [:session] #(-> session
                                      (merge (or % {:action [uri]}))
                                      (assoc-in [:action-v uri] params))))




(defn warp-navi-middleware [handler start-path]
  (fn [request]
    (let [{:keys [uri]} request]
      ;   (debug-request-params request)
      (if (= :post (:request-method request))
        (cond
          (= start-path uri)
          (init-session (handler request) request)
          (contains? (:params request) :prev)
          (-> (response/redirect (last (get-action request)))
              (assoc :session (remove-last-action (:session request))))
          :else
          (update-session (handler request) request))
        (handler request)))))



(defn get-action-value [r path]
  (get-in r [:session :action-v path]))

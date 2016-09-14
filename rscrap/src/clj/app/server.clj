(ns app.server
  (:require [immutant.web :as im]
            [clojure.tools.logging :as log]
            [app.state :as s]
            [app.routes :as r])
  (:gen-class))


(defonce server (atom nil))

(defn start-server [port ]
  (let [v (im/run r/http-handler {:port port
                                  :host "0.0.0.0"})]
    (reset! server v)))


(defn stop-server []
  (im/stop @server))




(defn -main
  [& args]
  (let [[port] args
        p (or port 3000)]
    (println "Starting server at  " p)
    (s/init-state)
    (im/run r/http-handler {:port p
                          :host "0.0.0.0"})))


(comment


  (im/run http-handler {:port 3000
                        ;:host "0.0.0.0"
                        })

  )
(ns app.server
  (:require [immutant.web :as im]
            [clojure.tools.logging :as log]
            [app.state :as s]
            [app.routes :as r])
  (:gen-class))


(def dev-server-config {:port 3001
                        ;:host "0.0.0.0"
                        })

(def prod-server-config {:port 9001
                         :host "0.0.0.0"})


(defonce server (atom nil))

(defn start-server [config]
  (let [v (im/run r/http-handler config)]
    (reset! server v)))


(defn stop-server []
  (im/stop @server))




(defn -main
  [& args]
  (let [[port] args
        p (or port 3000)]
    (println "Starting server at  " p)
    (s/init-state)
    (start-server prod-server-config)))


(comment


  (im/run http-handler {:port 3000
                        ;:host "0.0.0.0"
                        })

  )
(ns app.server
  (:require [immutant.web :as im]
            [clojure.tools.logging :as log]
            [app.state :as s]
            [app.routes :as r])
  (:gen-class))



#_(defn -main
  [& args]
  (println "Starting tie app  ")
  (s/init-state)
  (im/run r/http-handler {:port 3000
                        ;:host "0.0.0.0"
                        }))


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
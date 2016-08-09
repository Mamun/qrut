(ns app.service
  (:require [dadysql.re-frame :as tr]
            [ajax.core :as a]
            [dadysql.client :as client]))


(defn load-material []
  (->> (tr/build-ajax-request :material {})
       (client/default-ajax-params)
       (a/GET "/api/material" )))





;(js/console   (load-material))


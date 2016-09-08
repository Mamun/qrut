(ns app.handler.credittype
  (:require [net.cgrand.enlive-html :as html]
            [app.handler.credittype-view :as cv]
            [extractor.core :as e]
            [app.handler.common :as c]
            [extractor.util :as eu]))



(defn get-temp-data []
  (-> (e/extract-data "credittype.html")
      (get-in [:params])
      (eu/view-data)))


(defn view [submit-v]
  (let [d (get-temp-data)
        credit-line (cv/get-credit-line d)]
    (->> (cv/credittype-snippet d credit-line)
         (c/index-template "Credit type ")
         (apply str)
         (c/html-response))))





(comment


  (->> (get-temp-data)
       (get-credit-line)
       (html/emit*)
       (apply str))

  (let [[card & credit-line :as w] (->> (e/extract-data "credittype.html")
                                        (:params)
                                        (eu/view-data)
                                        (:credit-line))]
    (-> credit-line
        (credittype-line-snippet)
        #_(html/emit*)
        #_(apply str)))




  (credittype-line-card-snippet)


  (let [d (-> (e/extract-data "credittype.html")
              (:params)
              (eu/view-data))
        data [{:description      "Feste Rate"
               :instalmentsCount 36
               :instalment       "99,35"
               :interestRate     "9,9"
               :rsv              "RSV"
               }]
        ]

    (-> (credittype-snippet d)
        (html/select [:#credittype-table])
        (html/at [:tbody :tr] (html/clone-for [i (range 0 1)]
                                              [[:input (html/attr= :name "CALCULATION_TABLE")]] (html/set-attr :value i)
                                              ))
        (html/select [:tbody :tr])

        ))


  (view)


  (->
    (get-in
      (e/extract-data "credittype.html") [:params])
    (eu/view-format)
    ;(select-keys [:Instance_theDossierConditions_theMaterialInfo$0_mPrice] )

    )


  (let [p (get-in
            [:params])]
    (->> (eu/view-data p)
         (credittype-snippet)
         (html/emit*)
         (apply str)))

  )
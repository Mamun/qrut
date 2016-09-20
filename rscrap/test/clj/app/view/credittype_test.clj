(ns app.view.credittype-test
  (:require [net.cgrand.enlive-html :as html]
            [app.view.credittype :as cv]
            [app.view.core :as view]
            [scraper.core :as e]
            [scraper.util :as eu]))



#_(defn get-temp-data []
  (-> (e/scrap-data "credittype.html")
      (get-in [:params])
      (eu/view-data)))


#_(defn view [submit-v]
  (let [d (get-temp-data)
        credit-line (cv/get-credit-line d)]
    (->> (cv/credittype-snippet d credit-line)
         (c/index-template "Credit type ")
         (apply str)
         (c/html-response))))





(comment


  (html/html-resource "address.html")

  (->> (get-temp-data)
       (get-credit-line)
       (html/emit*)
       (apply str))

  #_(->
    (e/scrap-data "credittype.html")
    (view/view))


  #_(let [[card & credit-line :as w] (->> (e/scrap-data "credittype.html")
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
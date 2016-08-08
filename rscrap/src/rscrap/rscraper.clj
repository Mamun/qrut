(ns rscrap.rscraper
  (require                                                  ;[pl.danieljanus.tagsoup :as ts]
    [environ.core :as e]
    [net.cgrand.enlive-html :as html]
    [clj-http.client :as client]
    [rscrap.adapter :as a]
    [clojure.tools.reader.edn :as edn]
    [net.cgrand.tagsoup]))


(defn log [v]
  (println "-----")
  (clojure.pprint/pprint v)
  (println "Log done-------------- "))


(defn as-string [n]
  (reduce str (html/emit* n)))


(defn as-file [n]
  (spit "data.html" (as-string n)))



(def req-m (edn/read-string (slurp "params.edn")))


(comment




  (a/submit-page (a/login-page req-m) )


  (->
    (a/get-page (a/material-page req-m))


    )

  (do


    (a/submit-page (a/login-page req-m) )

    (-> (a/get-page (a/material-page req-m))
        (a/assoc-user-params req-m)
        (a/submit-page)

        (a/assoc-user-params req-m)
        (a/current-page)
        (a/submit-page)

        (a/assoc-user-params req-m)
        (a/submit-page)

        ;;custoemr identity
        (a/assoc-user-params req-m)
        (a/submit-page)

        ;;custoemr identity comple
        (a/assoc-user-params req-m)
        (a/submit-page)

        ;(:response)
        #_(as-file)
        #_(dissoc :response)))



  ;;Submit material



  ;;Submit credit type
  (do
    (->
      (a/get-page :credit)
      (eu/node->map))
    (-> {"Instance_theDossierConditions_theMaterialInfo$0_mPrice" 2000
         "CAM_mCreditAmount"                                      2000}
        (assoc "RequestID" "-80942774")
        (assoc "UNIQUE_TRANSACTION" "FLOWTID:2")
        (a/submit-page :credit)
        (eu/node->map)
        ;(dissoc "RequestID")
        #_(build-credit-request)
        ;   (assoc  "RequestID" "421401248")
        #_(a/submit-page :credit)
        #_(eu/node->map))

    )


  ;;Submit custoemr identitiy
  (do
    (->
      (a/get-page :customer)
      (eu/node->map)

      (build-customer-request)
      (assoc "RequestID" "421401845")
      ; (dissoc "RequestID" )
      (a/submit-page :customer)
      (eu/node->map)))












  (client/get
    "https://green-1.commerzfinanz.com/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2"
    {:cookie-store cs})






  (client/get "https://green-1.commerzfinanz.com/ratanet/front?controller=CreditApplication&action=Login&pstyle=ratanet")

  (clojure.pprint/pprint (clj-http.cookies/get-cookies cs))

  (client/get
    "https://green-1.commerzfinanz.com/ratanet/front?controller=CreditApplication&action=DispoMaterialType"
    {:cookie-store cs})

  )
;


;(System/getProperties)









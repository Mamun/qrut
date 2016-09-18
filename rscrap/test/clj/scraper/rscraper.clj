(ns extractor.rscraper
  (require                                                  ;[pl.danieljanus.tagsoup :as ts]
    [environ.core :as e]
    [net.cgrand.enlive-html :as html]
    [clj-http.client :as client]
    [scraper.remote-fetcher :as s]
    [app.handler.core :as v]
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



(def current-state (atom {}))


(defn store-state [v]
  (reset! current-state v))

(defn get-state [_]
  @current-state)







(comment


  (->
    (s/login-request)
    (s/fetch-data s/config)
    (s/init-flow-request)
    (s/fetch-data s/config)
   ; (v/view)

    )


  ;"/ratanet/front?controller=CreditApplication&action=PrintingContract"

  (-> (edn/read-string (slurp "credit_type_default.edn"))
      (assoc :debug? true)
      (a/create-contract "/ratanet/front?controller=CreditApplication&action=DispoPlusCreditType")
      (dissoc :response :node))


  (-> (edn/read-string (slurp "credit_type_default.edn"))
      (a/create-contract "/ratanet/front?controller=CreditApplication&action=DispoV2CustomerIdentity")
      (dissoc :response :node))




  (-> (edn/read-string (slurp "credit_type_default.edn"))
      (a/create-contract "/ratanet/front?controller=CreditApplication&action=PrintingContract")
      (dissoc :response :node))



  ;@curren

  (a/submit-page (a/login-page (edn/read-string (slurp "credit_type_default.edn"))))


  (->
    (a/get-page (a/material-page (edn/read-string (slurp "credit_type_default.edn"))))
    (dissoc :response))


  (do

    (a/submit-page (a/login-page req-m))




    ;(:params @current-state)
    ;(:url @current-state)

    ;FONT CLASS='errormessage'>


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











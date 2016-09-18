(ns app.view.common
  (:require [net.cgrand.enlive-html :as html]
            [scraper.core :as e]
            [scraper.util :as eu]))


(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


(html/defsnippet customer-snippet "public/customer.html"
                 [:div#customer]
                 []
                 identity)


(html/defsnippet customer-comple-snippet "public/customer_compl.html"
                 [:div#customer-compl]
                 []
                 identity)


;customer-identity-comple-view
(comment


  ;material_list
  (material-snippet)

  (html/html-resource "public/credit_type.html")


  (->> (credit-type-snippet)
       (main-template "Hello from credit type ")
       (apply str)
       )


  (apply str
         (html/emit*

           (html/snippet "public/templates/header.html"
                         [:header]
                         []
                         identity)

           )
         )



  (->>
    (main-template "Hello from word" [["Hello" " #"]])
    (html/emit*)
    (apply str)
    )

  )





#_(defn index []
    (->> (login-snippet)
         (index-template "Hello from credit type ")
         (apply str)
         (html-response)))





#_(defn login-view []
  (->>
    (login-template "Hello from credit type ")
    (apply str)
    (html-response)))


#_(defn customer-view []
  (->> (customer-snippet)
       (index-template "Hello from credit type ")
       (apply str)
       (html-response)))


#_(defn customer-comple-view []
  (->> (customer-comple-snippet)
       (index-template "Hello from credit type ")
       (apply str)
       (html-response)))

;(html/snippet "public/credit_type.html" [:body]  )

;(html/res)


(comment

  (index)

  (set-resource-path! (clojure.java.io/resource "public"))




  #_(html-response (io/input-stream (io/resource "public/index.html")))


  (defn index []
    (html-response
      (render-file "index.html" {:title "index"
                                 :navs  [{:link "index" :text "Home" :is-active "is-active"}
                                         {:link "contact" :text "Contact"}]})))

  (defn contact []
    (html-response
      (render-file "contact.html" {:title "Contact"
                                   :navs  [{:link "index" :text "Home"}
                                           {:link "contact" :text "Contact" :is-active "is-active"}]})))


  (defn admin-index []
    (html-response
      (render-file "admin_index.html" {:title "index"
                                       :navs  [{:link "index" :text "Home" :is-active "is-active"}
                                               {:link "contact" :text "Contact"}]}))))


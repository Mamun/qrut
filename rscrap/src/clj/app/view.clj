(ns app.view
  (:require [net.cgrand.enlive-html :as html ] ))


(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


(html/deftemplate index-template "public/index.html"
                  [title content]
                  [:head :title] (html/content title)
                  [:div#wrapper] (html/content content))


(html/defsnippet login-snippet "public/login.html"
                 [:div#login]
                 []
                 identity)


(html/defsnippet material-snippet "public/material.html"
                 [:div#material]
                 []
                 identity)


(html/defsnippet credittype-snippet "public/credittype.html"
                 [:div#credittype]
                 []
                 identity)


(html/defsnippet customer-identity-snippet "public/customer_identity.html"
                 [:div#customer-identity]
                 []
                 identity)


(html/defsnippet customer-identity-comple-snippet "public/customer_identity_compl.html"
                 [:div#customer-identity-compl]
                 []
                 identity)


;customer-identity-comple-view
(comment


  ;material_list
  (material-snippet)

  (html/html-resource "public/credit_type.html")


  (->>  (credit-type-snippet)
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


(defn html-response
  [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})



(defn index []
  (->>  (login-snippet)
        (index-template "Hello from credit type ")
        (apply str)
        (html-response)))



(defn material-view []
  (->>  (material-snippet)
        (index-template "Hello from credit type ")
        (apply str)
        (html-response)))



(defn credittype-view []
  (->>  (credittype-snippet)
        (index-template "Hello from credit type ")
        (apply str)
        (html-response)))


(defn customer-identity-view []
  (->>  (customer-identity-snippet)
        (index-template "Hello from credit type ")
        (apply str)
        (html-response)))


(defn customer-identity-comple-view []
  (->>  (customer-identity-comple-snippet)
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


(ns app.handler.common
  (:require [net.cgrand.enlive-html :as html]
            [extractor.core :as e]
            [extractor.util :as eu]))


(defn html-response
  [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})


(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


(html/deftemplate index-template "public/index.html"
                  [title content]
                  [:head :title] (html/content title)
                  [:div#wrapper] (html/content content))


(html/deftemplate login-template "public/login_template.html"
                  [title content]
                  [:head :title] (html/content title)
                  [:div#wrapper] (html/content content))


(html/defsnippet login-snippet "public/login.html"
                 [:div#login]
                 []
                 identity)







(comment


  (reduce str
          (html/emit*
            (login-snippet)))


  (select-keys
    (e/extract-data "material.html") [:params])


  (let [d
        {"612" "Telefon/Handy",
         "616" "Computer",
         "618" "Zubehör PC",
         "610" "TV/HIFI Geräte",
         "320" "Diverse Weiße Ware",
         "0"   "Kartenantrag ohne Kauf",
         "611" "Photo/Video",
         "322" "Kühlschrank oder Gefrierschrank",
         "323" "Spül-/Waschmaschine"}
        s {}]
    (-> (material-snippet d s)))

  )









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





(defn login-view []
  (->> (login-snippet)
       (login-template "Hello from credit type ")
       (apply str )
       (html-response)))


(defn customer-view []
  (->> (customer-snippet)
       (index-template "Hello from credit type ")
       (apply str)
       (html-response)))


(defn customer-comple-view []
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


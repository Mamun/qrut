(ns app.view
  (:require [net.cgrand.enlive-html :as html ] )
  #_(:use [selmer.parser]))


(html/set-ns-parser! net.cgrand.tagsoup/parser)



(html/deftemplate main-template "public/templates/application.html"
                  []
                  [:head :title] (html/content "Enlive starter kit"))



(html/defsnippet main-template "public/templates/header.html"
                 [:header]
                 [heading navigation-elements]
                 [:h1] (html/content heading)
                 [:ul [:li html/first-of-type]] (html/clone-for [[caption url] navigation-elements]
                                                                [:li :a] (html/content caption)
                                                                [:li :a] (html/set-attr :href url)))


(comment

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


(defn index-page [data]
  (-> (html/html-resource "public/index.html")
      (html/at [:#wrapper] (html/content "Display from data ") )))



(defn index []
  (->> (index-page {})
       (html/emit*)
       (reduce str)
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


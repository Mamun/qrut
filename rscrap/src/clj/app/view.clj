(ns app.view
  (:require [net.cgrand.enlive-html :as html]))


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
                 [material-m sales-m]
                 [:select#mcode [:option (html/but html/first-of-type)]] nil
                 [:select#mcode :option] (html/remove-attr :selected)
                 [:select#mcode :option]
                 (html/clone-for [i material-m]
                                 (html/do-> (html/content (second i))
                                            (html/set-attr :value (first i))))

                 [:select#salesmanid [:option (html/but html/first-of-type)]] nil
                 [:select#salesmanid :option] (html/remove-attr :selected)
                 [:select#salesmanid :option]
                 (html/clone-for [i sales-m]
                                 (html/do-> (html/content (second i))
                                            (html/set-attr :value (first i)))))


(defn apply-session [submit-m node ]
  (html/at node
           [:select#mcode [:option (html/attr= :value (get submit-m "Instance_theDossierConditions_theMaterialInfo$0_mCode") )]]
           (html/set-attr :selected :selected)))




(comment




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
    (-> (material-snippet d s)
        (apply-session)
        ))

  )




(html/defsnippet credittype-snippet "public/credittype.html"
                 [:div#credittype]
                 []
                 identity)



(comment

  (let []
    (-> (credittype-snippet) )
    )


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


(defn html-response
  [body]
  {:status  200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    body})



(defn index []
  (->> (login-snippet)
       (index-template "Hello from credit type ")
       (apply str)
       (html-response)))





(defn material-view [submit-m]
  (let [d {"612" "Telefon/Handy",
           "616" "Computer",
           "618" "Zubehör PC",
           "610" "TV/HIFI Geräte",
           "320" "Diverse Weiße Ware",
           "0"   "Kartenantrag ohne Kauf",
           "611" "Photo/Video",
           "322" "Kühlschrank oder Gefrierschrank",
           "323" "Spül-/Waschmaschine"}
        s {"mustermann" "Musterman"
           "212344"     "35t435"}]
    (->> (material-snippet d s)
         (apply-session submit-m)
         (index-template "Hello from credit type ")
         (apply str)
         (html-response))))



(defn credittype-view []
  (->> (credittype-snippet)
       (index-template "Hello from credit type ")
       (apply str)
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


(ns app.view.material
  (:require [net.cgrand.enlive-html :as html]
           [scraper.core :as scraper]
            #_[app.handler.common :as c]))


(html/set-ns-parser! net.cgrand.tagsoup/parser)
(html/alter-ns-options! assoc :reloadable? true)


#_(defn apply-session [submit-m node]
  (html/at node
           [:select#mcode [:option (html/attr= :value (get submit-m "Instance_theDossierConditions_theMaterialInfo$0_mCode"))]]
           (html/set-attr :selected :selected)
           [:select#salesmanid [:option (html/attr= :value (or
                                                             (get submit-m :Instance_theDossierConditions_theVendorInfo_mSalesmanId)
                                                             (get submit-m "Instance_theDossierConditions_theVendorInfo_mSalesmanId")))]]
           (html/set-attr :selected :selected)))


(html/defsnippet material-snippet2 "public/material.html"
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



(html/defsnippet material-snippet "public/material.html"
                 [:div#material]
                 [{:keys [m-code sales-man]}]
                 [:select#mcode ] (html/content m-code)
                 [:select#salesmanid ] (html/content sales-man)
                 )



#_(defn view [submit-m]
  (let [w (e/scrap-data "material.html")
        d (get-in w [:params "Instance_theDossierConditions_theMaterialInfo$0_mCode"])
        s (get-in w [:params "Instance_theDossierConditions_theVendorInfo_mSalesmanId"])]
    (->> (material-snippet d s)
         (apply-session submit-m)
         (c/index-template "Hello from credit type ")
         (apply str)
         (c/html-response))))

(comment
  (apply str
         (-> (scraper/as-node-map "public/old/material_old.html")
             (scraper/extract-content)
             (material-snippet)
             (html/emit*)
             ))

  )
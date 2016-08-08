(ns rscrap.adapter
  (require                                                  ;[pl.danieljanus.tagsoup :as ts]
    [environ.core :as e]
    [net.cgrand.enlive-html :as html]
    [clj-http.client :as client]
    [net.cgrand.tagsoup])
  (import [java.io StringReader]))


(defn select-tag [node]
  (->> (html/select node [:select])
       (map (fn [r]
              (let [p (get-in r [:attrs :name])
                    frr (filter #(not= "/n" %) (get-in r [:content]))
                    v (zipmap (map #(get-in % [:attrs :value]) frr)
                              (map #(html/text %) frr))]
                {p v})))
       (into {})))


(defn input-tag [v]
  (let [w1 (html/select v [:input])]
    (zipmap
      (map #(get-in % [:attrs :name]) w1)
      (map #(get-in % [:attrs :value]) w1))))



(defn get-from-fields [node]
  (reduce (fn [acc v]
            (merge acc (select-tag v) (input-tag v))
            ) {} node))



(defn get-form-url [node]
  (->> (html/select node [[:form (html/attr-has :method "post")]])
       (mapcat #(html/attr-values % :action))
       (first)))



(def cs (clj-http.cookies/cookie-store))
(html/set-ns-parser! net.cgrand.tagsoup/parser)


;(def ^:dynamic *base-url* "http://localhost:8021")





(defn as-response [r]
  (let [node (-> r
                 (:body)
                 (StringReader.)
                 (html/html-resource))
        params (get-from-fields node)
        form (get-form-url node)]
    {:params   params
     :url      form
     :response node}))


(defn get-page
  [{:keys [params url]}]
  (-> url
      (client/get {:cookie-store cs
                   :query-params params})
      (as-response)))



(defn submit-page [{:keys [params url debug? ]}]
  (when debug?
    (println "--Submit data start for --" url)
    (clojure.pprint/pprint params)
    (println "--Submit data end--"))
  (-> url
      (client/post {:form-params     params
                    :cookie-store    cs
                    :force-redirects true})
      (as-response)))



(defn login-page [config-m]
  {:params (get config-m "/ratanet/front?controller=CreditApplication&action=Login")
   :url    (str (get config-m "url" ) "/ratanet/front?controller=CreditApplication&action=Login")})


(defn material-page [config-m]
  {:params {}
   :url    (str (get config-m "url") "/ratanet/front?controller=CreditApplication&action=DispoMaterialType&ps=DISPOV2&init=1"   )})


(defn next-page [m]
  (-> m
      (update-in [:params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
      (update-in [:params] (fn [w] (assoc w "next.x" 14 "next.y" 14)))))


(defn previous-page [m]
  (-> m
      (update-in [:params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1")))
      (update-in [:params] (fn [w] (assoc w "prev.x" 35 "prev.y" 35)))))


(defn current-page [m]
  (update-in m [:params] (fn [w] (dissoc w "prev" "next" "alternate3" "alternate1"))))


(defn assoc-user-params [m config-m]
  (-> (merge-with merge m {:params (get config-m (:url m))})
      (update-in [:url] (fn [v] (str (get "url" config-m) v ) ) )
      (next-page)))


(comment

  (->> [[:form (html/attr-has :method "post")]]
       (html/select (html/html-resource "calc.html"))
       (mapcat #(html/attr-values % :action)
               ))

  )



;action=DispoV2CustomerIdentityComplementary


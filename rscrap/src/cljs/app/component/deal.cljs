(ns app.component.deal
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields init-field value-of]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [dadysql.ui :as v]
            [dadysql.re-frame :as tr]
            [app.service :as s]
            [app.component.ui :as appui]))



(def employee-template
  [:div.form-group
   [:input.form-control {:field       :numeric
                         :placeholder "Employee id "
                         :id          :search.id}]
   [:div.alert.alert-danger {:field :alert :id :errors.id}]])


(defn null-check []
  [:div {:class "checkbox"}
   [:label
    [:input {:type "checkbox"} "Null"]]])





(defn deal-list-view-template []
  (let [data (tr/subscribe [:material])]
    (fn []
        [:div.mdl-card
         [:ul.mdl-list
          [:li.mdl-list__item "Hello "]
          [:li.mdl-list__item "Hello2 "]]

         ]



      )))








(defn deal-view-template []
  [:div
   [deal-list-view-template]
   ])



(def deal-view
  (with-meta
    deal-view-template
    {:getInitialState #(s/load-material)})
  )

(ns frutil.spa.mui.item-selector
  (:require
   [reagent.core :as r]

   [reagent-material-ui.core.dialog :refer [dialog]]
   [reagent-material-ui.core.dialog-title :refer [dialog-title]]
   [reagent-material-ui.core.dialog-content :refer [dialog-content]]
   [reagent-material-ui.core.dialog-actions :refer [dialog-actions]]
   [reagent-material-ui.core.list :refer [list]]
   [reagent-material-ui.core.list-item :refer [list-item]]
   [reagent-material-ui.core.list-item-text :refer [list-item-text]]
   [reagent-material-ui.core.text-field :refer [text-field]]

   [frutil.spa.mui :as mui]))


(defn ItemSelector [{:keys [items]}]
  (let [all-items (->> items
                       (map #(assoc % :primary (or (get % :primary)
                                                   (str (get % :ident)))))
                       (map #(assoc % :match (str (-> % :primary .toLowerCase)
                                                  (.toLowerCase (or (get % :secondary)
                                                                    "")))))
                       (sort-by :primary))
        STATE (r/atom {:error nil
                       :items all-items})
        on-change (fn [text]
                    (let [text (-> text .trim)
                          items (if (= text "")
                                  all-items
                                  (->> all-items
                                       (filter #(-> % :match (.includes text)))))
                          error (when (empty? items)
                                  "No command matching.")]
                      (reset! STATE {:items items
                                     :error error})))]
    (fn [{:keys [on-item-selected list-style] :as _options}]
      (let [{:keys [error items]} @STATE]
        [:div.ItemSelector
         [:div.stack
          [text-field
           {:label "Command"
            :helper-text (or error "Type to search. ENTER to select.")
            :auto-focus true
            :on-change #(on-change (-> % .-target .-value))
            :error (boolean error)
            :variant :outlined
            :margin :dense
            :full-width true}]
          [list
           {:dense true
            :style (merge list-style
                          {:overflow :auto})}
           (for [item items]
             ^{:key (or (get item :ident) (get item :primary))}
             [list-item
              {:button true
               :on-click #(on-item-selected item)}
              [list-item-text
               {:primary (get item :primary)
                :secondary (get item :secondary)}]])]]]))))


(defn Dialog [{:keys [on-item-selected] :as options} dispose]
  [dialog
   {:open true
    :on-close dispose}
   [dialog-content
    [:div
     [ItemSelector
      (assoc options
             :list-style {:width "600px"
                          :max-width "50vw"
                          :height "600px"
                          :max-height "60vh"}
             :on-item-selected
             (fn [item]
               (dispose)
               (on-item-selected item)))]]]])

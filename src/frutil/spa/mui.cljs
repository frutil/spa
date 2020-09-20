(ns frutil.spa.mui
  (:require
   [cljs.pprint :as pprint]

   [reagent.core :as r]
   [reagent.dom :as rdom]

   [reagent-material-ui.core.css-baseline :refer [css-baseline]]
   [reagent-material-ui.styles :as styles]
   [reagent-material-ui.core.grid :refer [grid]]
   [reagent-material-ui.core.card :refer [card]]
   [reagent-material-ui.core.card-content :refer [card-content]]))


;;; JSS

(defn styled-component [styles component]
  (let [component-styles (fn [theme]
                           {:style (styles theme)})
        with-custom-styles (styles/with-styles component-styles)
        StyledComponent (fn [{:keys [classes] :as _props}]
                          [:div
                           {:class (:style classes)}
                           component])]
    [(with-custom-styles StyledComponent)]))


;;;


(defn Data
  [& datas]
  (into
   [:div.Data
    {:style {:display :grid
             :grid-gap "10px"}}]
   (map (fn [data]
          [:code
           {:style {:white-space :pre-wrap
                    :overflow :auto}}
           (try
             (with-out-str (pprint/pprint data))
             (catch :default ex
               "!!! ERROR !!! pprint failed for data"))])
        datas)))

;;; layouts

(defn Stack [options & components]
  (into
   [grid
    (assoc options
           :container true
           :direction :column
           :spacing (get options :spacing 1))]
   (map (fn [component]
          [grid
           {:item true}
           component])
        components)))

;;;

(defn Card [& children]
  [card
   [card-content
    (into
     [grid {:container true :direction :column :spacing 1}]
     (map (fn [child]
            [grid {:item true}
             child])
          children))]])


;;;

;;; dialogs


(defonce ACTIVE_DIALOG (r/atom nil))


(defn DialogsContainer []
  [:div.DialogsContainer
   (when-let [dialog @ACTIVE_DIALOG]
     (conj dialog
           #(reset! ACTIVE_DIALOG nil)))])


(defn show-dialog [dialog]
  (reset! ACTIVE_DIALOG dialog))


;;; app


(defn app [custom-theme custom-styles Content]
  [:<> ; fragment
   [css-baseline]
   [styles/theme-provider (styles/create-mui-theme custom-theme)
    (styled-component custom-styles [Content])]])


(defn mount-app [custom-theme custom-styles Content]
  (rdom/render (app custom-theme custom-styles Content)
               (js/document.getElementById "app")))

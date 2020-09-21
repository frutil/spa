(ns frutil.spa.mui
  (:require
   [clojure.string :as str]
   [cljs.pprint :as pprint]

   [reagent.core :as r]
   [reagent.dom :as rdom]

   [reagent-material-ui.core.css-baseline :refer [css-baseline]]
   [reagent-material-ui.styles :as styles]
   [reagent-material-ui.core.typography :refer [typography]]
   [reagent-material-ui.core.grid :refer [grid]]
   [reagent-material-ui.core.card :refer [card]]
   [reagent-material-ui.core.card-content :refer [card-content]]))


;;; JSS and Theme

(defn styled-component [styles component]
  (let [component-styles (fn [theme]
                           {:style (styles theme)})
        with-custom-styles (styles/with-styles component-styles)
        StyledComponent (fn [{:keys [classes] :as _props}]
                          [:div.StyledComponentWrapper
                           {:class (:style classes)}
                           component])]
    [(with-custom-styles StyledComponent)]))


(defonce THEME (r/atom nil))


(defn theme [& path]
  (get-in @THEME path))


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

;;; typography

(defn Caption [& texts]
  [typography
   {:variant :caption}
   (str/join " " texts)])


;;; layouts


(defn Desktop--Header-Content-Footer [header content footer]
  [:div.Desktop--Header-Content-Footer
   {:style {:height "100vh"
            :display :flex
            :flex-direction :column}}
   [:header
    {:style {:z-index 2}}
    header]
   [:div
    {:style {:height "100%"
             :overflow :auto
             :z-index 1
             :background-color (theme :palette :background :default)}}
    content]
   [:footer
    footer]])


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
  (let [custom-styles-wrapper
        (fn [theme]
          (reset! THEME theme)
          (js/console.log "MUI Theme:", theme)
          (custom-styles theme))]
    (rdom/render (app custom-theme custom-styles-wrapper Content)
                 (js/document.getElementById "app"))))

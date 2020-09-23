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
   [reagent-material-ui.core.card-content :refer [card-content]]
   [reagent-material-ui.components :as muic]

   [reagent-material-ui.icons.bug-report :refer [bug-report]]))


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


(defn HTML [html-code]
  [:div.HTML
   {:dangerouslySetInnerHTML {:__html html-code}}])


;;; exceptions


(defn- stack->hiccup [stack]
  (reduce (fn [div line]
            (conj div
                  [:div
                   {:style {:color (when (or
                                          (-> line
                                              (str/includes? "cljs.core.js"))
                                          (-> line
                                              (str/includes? "react_dom_development.js"))
                                          (-> line
                                              (str/includes? "reagent.ratom.js"))
                                          (-> line
                                              (str/includes? "reagent.dom.js"))
                                          (-> line
                                              (str/includes? "reagent.impl.component.js")))
                                     "#aaa")}}
                   (-> line
                       (str/replace #"@" " @ ")
                       (str/replace #"\$" " "))]))
          [:div]
          (-> stack (.split "\n"))))


(defn StackTrace [stack]
  (when stack
    [:div
     {:style {:font-family :monospace
              :white-space :pre-wrap
              :padding "1rem 0"}}
     (stack->hiccup
      (if (-> stack (.startsWith "\n"))
        (-> stack (.substring 1))
        stack))]))


(defn Exception [exception]
  (let [message (.-message exception)
        message (if message message (str exception))
        data (ex-data exception)
        cause (or (ex-cause exception) (.-cause ^js exception))
        stack (.-stack exception)]
    [:div.Exception
     (when cause
       [:div
        [Exception cause]
        [:div
         {:style {:margin "1rem 0"
                  :color :lightgrey
                  :font-style :italic}}
         "causes"]])
     [:div
      {:style {:font-weight :bold
               :letter-spacing "1px"}}
      (str message)]
     [StackTrace stack]
     (when-not (empty? data)
       (if (= ::error-boundary (-> data :error-type))
         [:div
          [StackTrace (get-in data [:info "componentStack"])]
          [Data (update data :info :dissoc "componentStack")]]
         [Data data]))]))


(defn ErrorCard [& contents]
  [card
   {:style {:background-color "#b71c1c" ; red 900
            :color "#ffffff"
            :min-width "400px"}}
   [card-content
    [:div
     {:style {:display :flex
              :overflow-x :auto}}
     [bug-report
      {:style {:margin-right "1rem"}}]
     (into [:div] contents)]]])


(defn ExceptionCard [exception]
  [ErrorCard
    (if exception
      [:div
       {:style {:margin "1em"}}
       [Exception exception]]
      [:div "A bug is making trouble :-("])])


(defn ErrorBoundary [comp]
  (if-not comp
    [:span]
    (let [!exception (r/atom nil)]
      (r/create-class
       {:component-did-catch
        (fn [this ex info]
          (let [cljs-react-class (-> comp first .-cljsReactClass)
                comp-name (if cljs-react-class
                            (-> cljs-react-class .-displayName)
                            "no-name")]
            (js/console.log
             "ErrorBoundary"
             "\nthis:" this
             "\ncomp" comp
             "\ncomp-name" comp-name
             "\nex:" ex
             "\ninfo:" info)
            (reset! !exception (ex-info (str "Broken component: " comp-name)
                                        {:error-type ::error-boundary
                                         :component comp
                                         :info (js->clj info)
                                         :react-class cljs-react-class}
                                        ex))))
        :reagent-render (fn [comp]
                          (if-let [exception @!exception]
                            [ExceptionCard exception]
                            comp))}))))


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
    [ErrorBoundary header]]
   [:div
    {:style {:height "100%"
             :overflow :auto
             :z-index 1
             :background-color (theme :palette :background :default)}}
    [ErrorBoundary content]]
   [:footer
    [ErrorBoundary footer]]])


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


(defn InfoDialog
  [{:keys [title]} children dispose]
  [muic/dialog
   {:open true
    :on-close dispose}
   (when title
     [muic/dialog-title title])
   [muic/dialog-content
    children]
   [muic/dialog-actions
    [muic/button
     {:color :primary
      :on-click dispose}
     "Ok"]]])


(defn ExceptionDialog [ex dispose]
  [InfoDialog
   {}
   [Exception ex]
   dispose])


(defn show-exception-dialog [ex]
  (show-dialog [ExceptionDialog ex]))


;;; app

(defn ErrorThrower []
  (throw (ex-info "Houston, we have a problem!"
                  {:problem "test error"}
                  (js/Error. "Some Dummy JavaScript Error."))))


(defn app [custom-theme custom-styles Content]
  [:<> ; fragment
   [css-baseline]
   [styles/theme-provider (styles/create-mui-theme custom-theme)
    (styled-component custom-styles [Content])]])




(defn App [root-component]
  [ErrorBoundary
   root-component])

(defn mount-app [custom-theme custom-styles Content]
  (let [custom-styles-wrapper
        (fn [theme]
          (reset! THEME theme)
          (js/console.log "MUI Theme:", theme)
          (custom-styles theme))]
    (rdom/render [App (app custom-theme custom-styles-wrapper Content)]
                 (js/document.getElementById "app"))))

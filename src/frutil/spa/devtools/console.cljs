(ns frutil.spa.devtools.console
  (:require
   [frutil.spa.devtools.spy :as spy]
   [frutil.spa.devtools.state :as state]))



(def panels
  [
   (spy/panel)
   (state/panel)])


(defn PanelActivationButton [panel]
  [:a
   {}
   (-> panel :id str)])


(defn Panel [panel]
  [:div.Panel
   (-> panel :component)])


(defn Panels []
  [:div.Panels
   {:style {:height "100%"
            :padding "0.25rem"}}
   [:nav
    {:style {:display :flex
             :gap "1rem"}}
    "Frutil DevTools Console"
    (for [panel panels]
      ^{:key (-> panel :id)}
      [PanelActivationButton panel])]
   [:div
    (for [panel panels]
      ^{:key (-> panel :id)}
      [:div
       [Panel panel]
       [:br] [:hr] [:br]])]
   [:br]])


(defn Console []
  [:div
   {:style {:height "300px"}}
   [:div
    {:style {:position :fixed
             :bottom 0
             :width "100%"
             :height "300px"
             :background-color "#DDD"
             :border-top "1px solid black"
             :overflow :auto}}
    [Panels]]])

(ns ^:figwheel-hooks frutil.spa.devtools.spy
  (:require
   [reagent.core :as r]

   [frutil.devtools :as devtools]

   [frutil.spa.devtools.ui :as ui]))


(defonce SPYS (r/atom '()))


(defn spy-impl [subject form identifier]
  (js/console.log "SPY" identifier form subject)
  (swap! SPYS conj [identifier form subject]))


(defn Spy [[identifier form subject]]
  [:div
   [:strong
    (-> identifier str)]
   [:div
    {:style {:display :flex
             :gap "1rem"}}
    [ui/Data form]
    [:div "=>"]
    [ui/Data subject]]])


(defn Panel []
  [:div
   {:style {:display :flex
            :flex-direction :column
            :gap "1rem"}}
   (for [spy @SPYS]
     ^{:key (-> spy first)}
     [ui/Card
      [Spy spy]])])


(defn panel []
  {:id :spy
   :component [#'Panel]})


(defn ^:before-load dev-before-load []
  (reset! SPYS '()))


(reset! devtools/SPY_IMPL spy-impl)

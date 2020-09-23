(ns frutil.spa.navigation
  (:require
   [reagent.core :as r]

   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rss]

   [frutil.spa.mui :as mui]))


(def href rfe/href)
(def push-state rfe/push-state)
(def replace-state rfe/replace-state)


(defonce MATCH (r/atom nil))


(defn match []
  @MATCH)


(defn params []
  (get-in @MATCH [:parameters :path]))


(defn param [k]
  (get-in @MATCH [:parameters :path k]))


(defn Switcher [component-key]
  [:div.Switcher
   {:class (str "Switcher--" component-key)}
   (when-let [match @MATCH]
     (when-let [component (get-in match [:data component-key])]
       [mui/ErrorBoundary
        [component match]]))])


(defn initialize! [routes]
  (rfe/start!
   (rf/router routes {:data {:coercion rss/coercion}})
   (fn [match _history]
     (reset! MATCH match))
   ;; set to false to enable HistoryAPI
   {:use-fragment true}))

(ns frutil.spa.devtools.state
  (:refer-clojure :exclude [Box])
  (:require
   [frutil.spa.state :as state]
   [frutil.spa.devtools.ui :as ui]))


(defn Box [box]
  (let [value (-> box :VALUE deref)
        status (-> box :STATUS deref)]
    [:div.Box
     [:hr]
     [:div.Box
      {:style {:display :flex
               :gap "1rem"}}
      [:div
       [:div [:strong (-> box :id str)]]
       (for [[k v] (-> status sort)]
         ^{:key k}
         [:div
          [:span (str k)]
          " "
          [:span (str v)]])]
      [ui/Data value]]]))


(defn Shelve [shelve]
  (let [boxes (-> shelve :BOXES deref vals)]
    [:div
     [:strong
      (-> shelve :id str)]
     ;; [ui/Data shelve]
     (for [box boxes]
       ^{:key (-> box :id)}
       [Box box])]))


(defn Panel []
  (let [shelves (vals @state/SHELVES)]
    [:div
     {:style {:display :flex
              :flex-direction :column
              :gap "1rem"}}
     (for [shelve (->> shelves (sort-by :id))]
       ^{:key (-> shelve :id)}
       [ui/Card
        [Shelve shelve]])]))


(defn panel []
  {:id :state
   :component [#'Panel]})

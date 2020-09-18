(ns frutil.spa.devtools.ui
  (:require
   [cljs.pprint :as pprint]))


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


(defn Card [content]
  [:div
   {:style {:background-color :white
            :border-radius "3px"
            :box-shadow "rgba(0, 0, 0, 0.2) 0px 2px 1px -1px, rgba(0, 0, 0, 0.14) 0px 1px 1px 0px, rgba(0, 0, 0, 0.12) 0px 1px 3px 0px"
            :padding "8px"}}
   content])

(ns frutil.spa.devtools.core)


(defonce CONSOLE (atom nil))


(defn Console []
  [:<>
   (when-let [console @CONSOLE]
     console)])


(defn initialize! [console]
  (reset! CONSOLE console))

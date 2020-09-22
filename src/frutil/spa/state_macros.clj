(ns frutil.spa.state-macros)


(defmacro def-state
  [sym options]
  (let [id (keyword (str (ns-name *ns*)) (str sym))]
    `(defonce ~sym (frutil.spa.state/reg-shelve
                    (assoc ~options :id ~id)))))

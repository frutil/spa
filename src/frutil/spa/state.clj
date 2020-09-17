(ns frutil.spa.state)


(defmacro def-state
  [sym options]
  (let [id (keyword (str (ns-name *ns*)) (str sym))]
    `(defonce ~sym (reg-shelve (merge {:id ~id}
                                      ~options)))))

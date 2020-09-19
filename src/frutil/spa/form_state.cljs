(ns frutil.spa.form-state)


(defn- assoc-ids-to-fields [form]
  (reduce (fn [form id]
            (assoc-in form [:field id :id] id))
          form (-> form :fields keys)))


(defn new-form-state [options]
  (-> options
      assoc-ids-to-fields))


(defn- validate-required [field]
  (when (-> field :required?)
    (let [v (-> field :value)]
      (when (or (nil? v)
                (= "" v))
        "Input required."))))


(defn validate-field [field]
  (if-not (-> field :touched?)
    field
    (assoc
     field :error
     (reduce (fn [error validator]
               (if error
                 error
                 (validator field)))
             nil
             (into [validate-required]
                   (-> field :validators))))))


(defn validate-fields [form]
  (reduce (fn [form field-id]
            (update-in form [:fields field-id] validate-field))
          form (-> form :fields keys)))


(defn validate-form [form]
  (-> form
      validate-fields))


(defn on-field-blur [form field-id]
  (-> form
      (assoc-in [:fields field-id :touched?] true)
      validate-form))


(defn touch-all-fields [form]
  (reduce (fn [form field-id]
            (assoc-in form [:fields field-id :touched?] true))
          form (-> form :fields keys)))


(defn on-submit [form callback]
  (-> form
      touch-all-fields
      validate-form))

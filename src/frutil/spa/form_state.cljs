(ns frutil.spa.form-state)


(defn new-form [options fields]
  (reduce (fn [form field]
            (let [id (get field :id)]
              (-> form
                  (assoc-in [:fields id] field)
                  (update :fields-order #(conj (or % []) id)))))
          options fields))


(defn fields-in-order [form]
  (mapv #(get-in form [:fields %]) (-> form :fields-order)))


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
      validate-fields
      (assoc :validated? true)))


(defn on-field-blur [form field-id]
  (-> form
      (assoc-in [:fields field-id :touched?] true)
      validate-form))


(defn touch-all-fields [form]
  (reduce (fn [form field-id]
            (assoc-in form [:fields field-id :touched?] true))
          form (-> form :fields keys)))


(defn valid? [form]
  (reduce (fn [valid field]
            (and valid
                 (-> field :error not)))
          true (-> form :fields)))


(defn on-submit [form]
  (let [form (-> form
                 (assoc :submitted? true)
                 touch-all-fields
                 validate-form)]
    form))

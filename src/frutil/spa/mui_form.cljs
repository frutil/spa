(ns frutil.spa.mui-form
  (:require
   [reagent.core :as r]

   [reagent-material-ui.core.text-field :refer [text-field]]

   [frutil.spa.form-state :as form-state]
   [frutil.spa.mui :as mui]))


(defmulti create-field (fn [field-options] (get field-options :field-type)))

(defn Form
  [options & children]
  (let [FORM_STATE (r/atom (form-state/new-form-state
                            (-> options :state)))]
    (fn [options & children]
      (let [form-state @FORM_STATE]
        [:form
         (into
          [:div]
          (map (fn [child]
                 (if (keyword? child)
                   (let [field (get-in form-state [:fields child])]
                     (create-field (assoc field :FORM_STATE FORM_STATE)))
                   child))
               children))
         [:hr]
         [mui/Data form-state]]))))
         ;; (for [field (-> options :fields)]
         ;;   ^{:key (-> field :id)}
         ;;   (form-field (assoc field :FORM_STATE FORM_STATE)))]))))


(defn TextField
  [{:keys [id FORM_STATE] :as options}]
  ;; TODO mandatory id, FORM_STATE
  (let [error (get options :error)
        helper-text (if error
                      error
                      (get options :helper-text))]
    [text-field
     {:name id
      :label (or (get options :label) (str id))
      :helper-text helper-text
      :auto-focus (get options :auto-focus?)
      :required (get options :required?)
      :on-blur #(swap! FORM_STATE form-state/on-field-blur id)
      :on-change #(swap! FORM_STATE
                         assoc-in [:fields id :value]
                         (-> % .-target .-value))
      :error (boolean error)
      :variant :outlined
      :margin :dense
      :full-width true}]))

(defmethod create-field :text [options] [TextField options])

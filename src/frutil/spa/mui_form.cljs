(ns frutil.spa.mui-form
  (:require
   [reagent.core :as r]

   [reagent-material-ui.components :as muic]

   [frutil.spa.form-state :as form-state]
   [frutil.spa.mui :as mui]))


(defmulti create-field (fn [field-options] (get field-options :field-type)))

(defn Form
  [form]
  (let [FORM_STATE (r/atom form)
        on-submit (fn []
                    (swap! FORM_STATE form-state/on-submit)
                    (let [form @FORM_STATE]
                      (when (form-state/valid? form)
                        ((get form :on-submit) form)))
                    false)]
    (fn [_form]
      (let [form @FORM_STATE]
        [:form
         {:on-submit (fn [event]
                       (on-submit)
                       (-> event .preventDefault))}
         (for [[index field] (map-indexed (fn [index field] [index field])
                                          (form-state/fields-in-order form))]
           ^{:key (get field :id)}
           [:div
            (create-field (assoc field
                                 :auto-focus? (= 0 index)
                                 :FORM_STATE FORM_STATE))])
         [:div
          {:style {:display :flex
                   :justify-content :flex-end
                   :gap "8px"
                   :padding-top "8px"}}
          [muic/button
           {:color :primary
            :on-click #(when-let [on-cancel (get form :on-cancel)]
                         (on-cancel form))}
           "Cancel"]
          [muic/button
           {:color :secondary
            :variant :contained
            :on-click on-submit}
           "Submit"]]]))))
         ;; [:hr]
         ;; [mui/Data form]]))))


(defn FormDialog
  [form dispose]
  (let [real-on-submit (get form :on-submit)
        _ (when-not real-on-submit (throw (ex-info "missing :on-submit"
                                                   {:form form})))
        on-submit (fn [form]
                    (dispose)
                    (real-on-submit form))
        form (assoc form
                    :on-submit on-submit
                    :on-cancel dispose)]
    [muic/dialog
     {:open true
      :on-close dispose}
     [muic/dialog-content
      [Form form]]]))



(defn show-form-dialog [options & fields]
  (mui/show-dialog
   [FormDialog (form-state/new-form options fields)]))


(defn TextField
  [{:keys [id FORM_STATE] :as options}]
  ;; TODO mandatory id, FORM_STATE
  (let [error (get options :error)
        helper-text (if error
                      error
                      (get options :helper-text))]
    [muic/text-field
     {:name id
      :default-value (get-in @FORM_STATE [:fields id :value])
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

(ns frutil.spa.state
  (:require-macros [frutil.spa.state])
  (:require
   [reagent.core :as r]

   [frutil.spa.localstorage :as localstorage]
   [frutil.spa.navigation :as navigation]))


(defn timestamp []
  ;; FIXME
  (-> (js/Date.) .getTime))


(defonce SHELVES (r/atom {}))


(declare value)


(defn- load-from-localstorage [shelve item-id]
  [(localstorage/get-item [:state :value (get shelve :ident) item-id])
   (localstorage/get-item [:state :etag (get shelve :ident) item-id])])


(defn- save-to-localstorage [shelve item-id value etag]
  (let [serialize (get shelve :localstorage-save-transform-f identity)
        value (serialize value)]
    (js/console.log "SAVE:" value)
    (localstorage/set-item [:state :value (get shelve :ident) item-id] value)
    (localstorage/set-item [:state :etag (get shelve :ident) item-id] etag)))


(defn- initialize-load-save [shelve]
  (if (or (get shelve :load-f)
          (get shelve :save-f))
    shelve
    (if-not (get shelve :localstorage?)
      shelve
      (assoc shelve
             :load-f load-from-localstorage
             :save-f save-to-localstorage))))


(defn reg-shelve [options]
  (js/console.log "reg-shelve:" options)
  (let [id (get options :id)
        shelve (assoc options
                      :ident (get options :ident id)
                      :BOXES (r/atom {}))
        shelve (initialize-load-save shelve)]
    (swap! SHELVES assoc id shelve)
    (with-meta
      (partial value shelve)
      {::shelve shelve})))


(defn shelve [state-fn]
  (-> state-fn meta ::shelve))


(defn- new-box [item-id]
  {:id item-id
   :VALUE (r/atom nil)
   :STATUS (r/atom {})})


(defn on-update-received [shelve item-id box value etag]
  (let [STATUS (get box :STATUS)
        VALUE (get box :VALUE)]
    (reset! VALUE value)
    (swap! STATUS #(-> %
                       (assoc :etag etag)
                       (assoc :received (timestamp))
                       (dissoc :requested)))
    (when-let [save (get shelve :save-f)]
      (save shelve item-id value etag)
      (swap! STATUS assoc :saved timestamp)))
  nil)


(defn- request-update-for-box! [shelve item-id box]
  (when-let [request (get shelve :request-f)]
    (let [STATUS (get box :STATUS)
          etag (get @STATUS :etag)]
      (swap! STATUS assoc :requested (timestamp))
      (request shelve item-id etag (partial on-update-received shelve item-id box))))
  shelve)


(defn box [shelve item-id]
  (let [BOXES (get shelve :BOXES)]
    (or (get @BOXES item-id)
        (let [box (new-box item-id)]
          (swap! BOXES assoc item-id box)
          (when-let [load (get shelve :load-f)]
            (let [[value etag] (load shelve item-id)
                  STATUS (get box :STATUS)
                  VALUE (get box :VALUE)]
              (reset! VALUE value)
              (swap! STATUS #(assoc %
                                    :loaded (timestamp)
                                    :etag etag))))
          (request-update-for-box! shelve item-id box)
          box))))


(defn request-update! [shelve item-id]
  (request-update-for-box! shelve item-id (box shelve item-id)))


(defn value
  ([shelve]
   (value shelve :singleton))
  ([shelve item-id]
   (let [box (box shelve item-id)
         VALUE (get box :VALUE)]
     @VALUE)))


(defn status [shelve item-id]
  (let [box (box shelve item-id)
        STATUS (get box :STATUS)]
    @STATUS))


(defn update! [state-fn item-id update-fn & args]
  (let [etag nil ;FIXME
        shelve (shelve state-fn)
        timestamp (timestamp)
        box (box shelve item-id)
        STATUS (get box :STATUS)
        VALUE (get box :VALUE)]
    (swap! STATUS #(assoc %
                          :set timestamp
                          :etag etag))
    (swap! VALUE #(apply update-fn % args))
    (when-let [save (get shelve :save-f)]
      (save shelve item-id @VALUE etag)
      (swap! STATUS assoc :saved timestamp)))
  nil)


(defn set! [state-fn item-id value etag]
  ;; FIXME etag
  (update! state-fn item-id (fn [_old-value] value)))


(defn clear-all! [state-fn]
  (let [shelve (shelve state-fn)
        BOXES (get shelve :BOXES)]
    (reset! BOXES {})))


;;; pointers


(defn new-pointer [state-fn resolve-key-fn]
  (fn []
    (let [k (resolve-key-fn)]
      (state-fn k))))


(defn new-navigation-param-pointer [state-fn param]
  (new-pointer state-fn #(navigation/param param)))

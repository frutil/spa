(ns frutil.spa.state
  (:require
   [reagent.core :as r]))


(defn timestamp []
  ;; FIXME
  (-> (js/Date.) .getTime))


(defonce SHELVES (r/atom {}))


(defn reg-shelve [options]
  (let [id (get options :id)
        shelve (assoc options
                      :BOXES (r/atom {}))]
    (swap! SHELVES assoc id shelve)
    shelve))


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


(defn set! [shelve item-id value etag]
  (let [timestamp (timestamp)
        box (box shelve item-id)
        STATUS (get box :STATUS)
        VALUE (get box :VALUE)]
    (swap! STATUS #(assoc %
                          :set timestamp
                          :etag etag))
    (reset! VALUE value)
    (when-let [save (get shelve :save-f)]
      (save shelve item-id value etag)
      (swap! STATUS assoc :saved timestamp)))
  shelve)

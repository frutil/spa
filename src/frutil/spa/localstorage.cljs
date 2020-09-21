(ns frutil.spa.localstorage
  (:require
   [frutil.util :as u]))


(defn encode-key [k]
  (if (string? k) k (u/encode-edn k)))


(defn get-item [k]
  (try
    (-> js/window
        .-localStorage
        (.getItem (encode-key k))
        u/decode-edn)
    (catch :default ex
      (throw (ex-info "frutil.spa.localstorage/get-item failed"
                      {:key k}
                      ex)))))


(defn set-item [k v]
  (-> js/window
      .-localStorage
      (.setItem (encode-key k)
                (u/encode-edn v))))


(defn subscribe [k callback]
  (-> js/window
      (.addEventListener
       "storage"
       (fn [event]
         (when (= (encode-key k)
                  (-> event .-key))
           (callback (u/decode-edn (-> event .-newValue))))))))


(defn subscribe-to-clear [callback]
  (-> js/window
      (.addEventListener
       "storage"
       (fn [event]
         (when-not (-> event .-key)
           (callback))))))

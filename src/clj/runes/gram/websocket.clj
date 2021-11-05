(ns runes.gram.websocket
  (:require
   [runes.core :as runes]
   [gram.websocket]))

(defmethod runes/expand ::get [_ opts]
  (assoc opts :gram/server (runes/ref ::server)))

(defmethod runes/start ::get [_ _opts]
  gram.websocket/handler-get)

(defmethod runes/pause ::get [_ _opts])

(defmethod runes/resume ::get [_key _opts _old-opts old-impl]
  old-impl)

(defmethod runes/expand ::post [_ opts]
  (assoc opts :gram/server (runes/ref ::server)))

(defmethod runes/start ::post [_ _opts]
  gram.websocket/handler-post)

(defmethod runes/pause ::post [_ _opts])

(defmethod runes/resume ::post [_key _opts _old-opts old-impl]
  old-impl)

(defmethod runes/expand ::server [_ opts]
  (if (:sente/adapter opts)
    opts
    (assoc opts :sente/adapter (runes/ref :sente/adapter))))

(defmethod runes/start ::server [_ opts]
  (gram.websocket/start! (:sente/adapter opts)))

(defmethod runes/pause ::server [_ _opts])

(defmethod runes/resume ::server [_key _opts _old-opts old-impl]
  (gram.websocket/broadcast! :gram/reload)
  old-impl)

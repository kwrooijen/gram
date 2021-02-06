(ns duct.gram.websocket
  (:require
   [integrant.core :as ig]
   [gram.websocket]))

(defmethod ig/prep-key :duct.gram.websocket/get [_ opts]
  (assoc opts :gram/server (ig/ref :duct.gram.websocket/server)))

(defmethod ig/init-key :duct.gram.websocket/get [_ _opts]
  gram.websocket/handler-get)

(defmethod ig/suspend-key! :duct.gram.websocket/get [_ _opts])

(defmethod ig/resume-key :duct.gram.websocket/get [_key _opts _old-opts old-impl]
  old-impl)


(defmethod ig/prep-key :duct.gram.websocket/post [_ opts]
  (assoc opts :gram/server (ig/ref :duct.gram.websocket/server)))

(defmethod ig/init-key :duct.gram.websocket/post [_ _opts]
  gram.websocket/handler-post)

(defmethod ig/suspend-key! :duct.gram.websocket/post [_ _opts])

(defmethod ig/resume-key :duct.gram.websocket/post [_key _opts _old-opts old-impl]
  old-impl)

(defmethod ig/prep-key :duct.gram.websocket/server [_ opts]
  (if (:sente/adapter opts)
    opts
    (assoc opts :sente/adapter (ig/ref :sente/adapter))))

(defmethod ig/init-key :duct.gram.websocket/server [_ opts]
  (gram.websocket/start! (:sente/adapter opts)))

(defmethod ig/suspend-key! :duct.gram.websocket/server [_ _opts])

(defmethod ig/resume-key :duct.gram.websocket/server [_key _opts _old-opts old-impl]
  (gram.websocket/broadcast! :gram/reload)
  old-impl)

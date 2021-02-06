(ns gram.websocket
  (:require
   [taoensso.sente :as sente]))

(defonce ^:dynamic ring-ajax-post nil)
(defonce ^:dynamic ring-ajax-get-or-ws-handshake nil)
(defonce ^:dynamic ch-chsk nil)
(defonce ^:dynamic chsk-send! nil)
(defonce ^:dynamic connected-uids nil)

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))

(defmethod -event-msg-handler :chsk/ws-ping [_])

(defmethod -event-msg-handler
  :chsk/uidport-close
  [{:keys [event uid id ring-req]}]
  (let [session (:session ring-req)]))

(defmethod -event-msg-handler
  :chsk/uidport-open
  [{:keys [uid]}])

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defn start! [adapter]
  (let [chsk-server (sente/make-channel-socket-server!
                     adapter
                     {:packer :edn
                      :user-id-fn (comp :uid :session)})
        {:keys [ch-recv send-fn connected-uids ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
    (alter-var-root #'gram.websocket/ring-ajax-post                (constantly ajax-post-fn))
    (alter-var-root #'gram.websocket/ring-ajax-get-or-ws-handshake (constantly ajax-get-or-ws-handshake-fn))
    (alter-var-root #'gram.websocket/ch-chsk                       (constantly ch-recv))
    (alter-var-root #'gram.websocket/chsk-send!                    (constantly send-fn))
    (alter-var-root #'gram.websocket/connected-uids                (constantly connected-uids))
    (sente/start-server-chsk-router! ch-chsk event-msg-handler)))

(defn handler-get [request]
  (ring-ajax-get-or-ws-handshake request))

(defn handler-post [request]
  (ring-ajax-post request))

(defn broadcast!
  ([event-type] (broadcast! event-type {}))
  ([event-type data]
   (doseq [uid (:ws @connected-uids)]
     (chsk-send! uid [event-type data]))))

(ns gram.websocket
  (:require
   [turbo.core]
   [taoensso.sente  :as sente]))

(defonce ^:dynamic chsk nil)
(defonce ^:dynamic ch-chsk nil)
(defonce ^:dynamic chsk-send! nil)
(defonce ^:dynamic chsk-state nil)

(defn ?csrf-token []
  (-> (js/document.querySelector "[data-csrf-token]")
      (.getAttribute "data-csrf-token")))

(defmulti handle-event (fn [k _data] k))

(defmethod handle-event :gram/reload [__ ]
  (turbo.core/visit! js/window.location.href))

(defmethod handle-event :chsk/ping [_ _])

(defmethod handle-event :default [e _] (println "Unhandled event: " e))

(defmulti -event-msg-handler :id)

(defn event-msg-handler [ev-msg] (-event-msg-handler ev-msg))

(defmethod -event-msg-handler :default [{:as ev-msg :keys [event]}]
  (println "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/recv [{:as ev-msg :keys [?data]}]
  (apply handle-event ?data))

(defmethod -event-msg-handler :chsk/handshake [_])

(defmethod -event-msg-handler :chsk/state [_])

(defonce router (atom nil))

(defn stop-router! [] (when-let [stop-f @router] (stop-f)))

(defn start-router! []
  (stop-router!)
  (let [client (sente/make-channel-socket-client! "/chsk" (?csrf-token))]
  (set! chsk       (:chsk client))
  (set! ch-chsk    (:ch-recv client))
  (set! chsk-send! (:send-fn client))
  (set! chsk-state (:state client)))
  (reset! router
    (sente/start-client-chsk-router!
      ch-chsk -event-msg-handler)))

(defn start! [] (start-router!))

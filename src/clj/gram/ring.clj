(ns gram.ring
  (:require
   [turbo.ring :refer [wrap-turbo-frame wrap-turbo-stream]]
   [hiccup.core :as hiccup]))

(defn wrap-hiccup [handler]
  (fn [request]
    {:headers {"Content-Type" "text/html"}
     :body (hiccup/html (handler request))}))

(defn wrap-gram
  ([handler] (wrap-gram handler {}))
  ([handler opts]
   (fn [request]
     ((-> handler
          (wrap-turbo-frame)
          (wrap-turbo-stream)
          (wrap-hiccup))
      request))))

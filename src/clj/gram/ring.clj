(ns gram.ring
  (:require
   [clojure.string :as string]
   [turbo.ring :refer [wrap-turbo-frame wrap-turbo-stream]]
   [gram.html]))

(defn- add-csrf [content request]
  (if-let [csrf-token (:anti-forgery-token request)]
    (conj [:meta {:id "csrf" :data-csrf-token csrf-token}] content)
    content))

(defn- check
  [result]
  (and (vector? (:body result))
       (or (nil? (:headers result))
           (-> (get-in result [:headers "Content-Type"] "")
               (string/includes? "html")))))

(defn wrap-render
  ([handler] (wrap-render handler {}))
  ([handler opts]
   (fn [request]
     (let [result (handler request)
           uid (str (java.util.UUID/randomUUID))]
       (cond
         (vector? result)
         {:headers {"Content-Type" "text/html"}
          :session {:uid uid}
          :body (gram.html/html opts (add-csrf result request))}
         (check result)
         (-> result
             (update :body (fn [body] (gram.html/html opts (add-csrf body request))))
             (assoc-in [:headers "Content-Type"] "text/html")
             (assoc :session (:session request))
             (update-in [:session :uid] (fn [uuid] (java.util.UUID/randomUUID))))
         :else result)))))

(defn wrap-gram
  ([handler] (wrap-gram handler {}))
  ([handler _opts]
   (fn [request]
     ((-> handler
          (wrap-turbo-frame)
          (wrap-turbo-stream)
          (wrap-render))
      request))))

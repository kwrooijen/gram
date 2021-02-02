(ns gram.ring
  (:require
   [clojure.string :as string]
   [turbo.ring :refer [wrap-turbo-frame wrap-turbo-stream]]
   [gram.html]))

(defn wrap-hiccup [handler]
  (fn [request]
    (let [result (handler request)]
      (cond
        (vector? result)
        {:headers {"Content-Type" "text/html"}
         :body (gram.html/html result)}

        (and (vector? (:body result))
             (or (nil? (:headers result))
                 (-> (get-in result [:headers "Content-Type"] "")
                     (string/includes? "html"))))
        (-> result
            (update :body (fn [body] (gram.html/html body)))
            (assoc-in [:headers "Content-Type"] "text/html"))

        :else result))))

(defn wrap-gram
  ([handler] (wrap-gram handler {}))
  ([handler _opts]
   (fn [request]
     ((-> handler
          (wrap-turbo-frame)
          (wrap-turbo-stream)
          (wrap-hiccup))
      request))))

(ns runes.sente.adapter.http-kit
  (:require
   [runes.core :as runes]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defmethod runes/start :runes.sente.adapter/http-kit [_ _opts]
  (get-sch-adapter))

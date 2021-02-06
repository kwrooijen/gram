(ns duct.sente.adapter.http-kit
  (:require
   [integrant.core :as ig]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defmethod ig/init-key :duct.sente.adapter/http-kit [_ _opts]
  (get-sch-adapter))

(ns gram.html
  (:require [clojure.string :as string]))

(def default-attribute-map
  {"." :class
   "#" :id
   "!" :excl
   "$" :dollar
   "%" :perc
   "&" :amp
   "*" :ast})

(defn input-checkbox [opts & children]
  [:input (assoc opts :type :checkbox)
   children])

(def default-element-map
  {:input/checkbox input-checkbox})

(def default-formatter-map
  {:class #(pr-str (string/join " " (mapv name (filter identity (flatten %)))))
   :id #(pr-str (string/join " " (mapv name (filter identity (flatten %)))))
   :type (comp pr-str name)
   :autocomplete (comp pr-str name)
   :for (comp pr-str name)
   :value (comp pr-str name)})

(defn- default-formatter [v]
  (if (or (string? v)
          (number? v)
          (coll? v))
    (pr-str v)
    (pr-str (pr-str v))))

(defn- tag-matcher [attributes]
  (re-pattern
   (format "(?=(%s))"
           (reduce (fn [acc x]
                     (format "%s|\\%s" acc (first x)))
                   (format "\\%s" (ffirst attributes))
                   (rest attributes)))))

(def default-opts
  {:gram/attributes default-attribute-map
   :gram/elements default-element-map
   :gram/formatters default-formatter-map
   :gram/matcher (tag-matcher default-attribute-map)})

(defn update-matcher [{:gram/keys [attributes] :as m}]
  (if (= default-attribute-map attributes)
    m
    (assoc m :gram/matcher (tag-matcher attributes))))

(defn- parse-tag [{:gram/keys [attributes matcher]} tag]
  (let [[tag-name & xs] (string/split (name tag) matcher)]
    [(if (qualified-keyword? tag)
       (keyword (namespace tag) tag-name)
       (keyword tag-name))
     (reduce (fn [acc x]
               (if-let [attribute-type (get attributes (subs x 0 1))]
                 (update acc attribute-type conj (subs x 1))
                 acc))
             {}
             xs)]))

(defn- flatten-children [children]
  (cond
    (and (= (count children) 1)
         (seq? (first children)))
    (first children)
    (not (seq? children))
    (list children)
    :else
    children))

(defn- format-attribute [opts acc [k v]]
  (format "%s %s=%s" acc (name k) ((get-in opts [:gram/formatters k] default-formatter) v)))

(defn- opts->html [opts html-opts]
  (reduce (partial format-attribute opts)"" html-opts))

(defn- extract-opts-children
  [?html-opts children]
  (cond
    (map? ?html-opts)
    [?html-opts (flatten-children children)]
    (nil? children)
    [{} (flatten-children ?html-opts)]
    :else
    [{} (cons ?html-opts (flatten-children children))]))

(defmulti to-html
  (fn [_opts expr]
    (cond
      (vector? expr) :element
      (list? expr) :list
      :else :value)))

(defn- concat-merge [opts extra]
  (reduce (fn [acc [k v]] (update acc k concat v)) opts extra))

(defmethod to-html :element [opts [k ?html-opts & children]]
  (let [[html-opts children] (extract-opts-children ?html-opts children)
        [tag tag-opts] (parse-tag opts k)
        html-opts (concat-merge html-opts tag-opts)]
    (if (qualified-keyword? tag)
      (-> (get-in opts [:gram/elements tag])
          (apply html-opts children)
          (->> (to-html opts)))
      (let [html-opts (opts->html opts html-opts)
            children (apply str (mapv (partial to-html opts) children))
            tag (name tag)]
        (format "<%s%s>%s</%s>" tag html-opts children tag)))))

(defmethod to-html :list [opts v]
  (apply str (mapv (partial to-html opts) v)))

(defmethod to-html :default [_opts v]
  v)

(defn html
  "TODO"
  ([form]
   (to-html default-opts form))
  ([{:gram/keys [elements attributes formatters]} form]
   (to-html (-> default-opts
                (update :gram/elements merge elements)
                (update :gram/attributes merge attributes)
                (update :gram/formatters merge formatters)
                (update-matcher))
            form)))

(comment

  ;; TODO
  (html [:div {:id "foo"}])
  ;; "<div id=\"\"></div>"

  ,)

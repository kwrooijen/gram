(defproject kwrooijen/gram "0.0.1-SNAPSHOT"
  :description "A server-side rendering framework based on Hotwire"
  :url "https://github.com/kwrooijen/gram"
  :license {:name "MIT"}
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :dependencies [[kwrooijen/turbo "0.0.1-SNAPSHOT"]
                 [kwrooijen/stimulus "0.0.1-SNAPSHOT"]
                 [com.taoensso/sente "1.16.1"]
                 [integrant "0.8.0"]]
  :plugins [[lein-cloverage "1.1.2"]
            [lein-codox "0.10.7"]
            [lein-ancient "0.6.15"]]

  :repositories [["public-github" {:url "git://github.com"}]
                 ["private-github" {:url "git://github.com" :protocol :ssh}]]

  :codox {:doc-files ["README.md"]
          :output-path "docs/"
          :html {:namespace-list :nested}
          :metadata {:doc/format :markdown}
          :themes [:rdash]}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [orchestra "2020.09.18-1"]
                                  [codox-theme-rdash "0.1.2"]]}
             :test {:dependencies [[orchestra "2020.09.18-1"]]}}
  :deploy-repositories [["releases" :clojars]])

(defproject clj-comgate "0.0.0"
  :description "A client for Comgate Payments API"
  :url "https://github.com/druids/clj-comgate"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[http-kit "2.2.0"]
                 [prismatic/schema "1.1.9"]
                 [tol "0.8.0"]]

  :profiles {:dev {:plugins [[lein-cloverage "1.0.10"]
                             [lein-kibit "0.1.6"]
                             [jonase/eastwood "0.2.5"]
                             [venantius/ultra "0.5.2"]]
                   :dependencies [[org.clojure/clojure "1.9.0"]]
                   :source-paths ["src" "dev/src"]}
             :test {:dependencies [[org.clojure/clojure "1.9.0"]
                                   [http-kit.fake "0.2.2"]]}}
  :aliases {"coverage" ["with-profile" "test" "cloverage" "--fail-threshold" "95" "-e" "dev|user"]})

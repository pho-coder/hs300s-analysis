(defproject hs300s-analysis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [incanter/incanter-core "1.5.7"]
                 [org.clojure/data.csv "0.1.3"]
                 [com.opencsv/opencsv "3.8"]
                 [mount "0.1.11"]
                 [cprop "0.1.9"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]]
  :main hs300s-analysis.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

(ns hs300s-analysis.core
  (:require [mount.core :as mount]
            [clojure.tools.logging :as log]
            
            [hs300s-analysis.config :refer [env]]
            [hs300s-analysis.utils :as utils]
            [hs300s-analysis.analysis :as analysis]
            [hs300s-analysis.utils :as utils])
  (:gen-class))

(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn check-finish [path]
  (try
    (let [finish-file (clojure.java.io/as-file (str path "/finish"))]
      (.exists finish-file))
    (catch Exception e
      (log/warn e)
      false)))

(defn run [dt]
  (let [path (:data-path env)
        dt (:dt env)
        hs300-download-finish? (check-finish (str path "/" dt))]
    (if hs300-download-finish?
      (do (let [summary-path (str path "/../summary/" dt)]
            (if-not (.exists (clojure.java.io/as-file summary-path))
              (clojure.java.io/make-parents (str summary-path "/test")))
            (let [summary (analysis/summary-one-day path dt)]
              (utils/write-csv (:all summary)
                               (str summary-path "/all"))
              (log/info "write all csv")
              (utils/write-csv (:afternoon summary)
                               (str summary-path "/afternoon"))
              (log/info "write afternoon csv")
              (utils/write-csv (:volume-400 summary)
                               (str summary-path "/volume-400"))
              (log/info "write volume-400 csv")
              (utils/write-csv (:amount-10 summary)
                               (str summary-path "/amount-10"))
              (log/info "write amount-10 csv")
              false)))
      true)))

(defn start-app [args]
  (doseq [component (-> args
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (log/info env)
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app))
  (let [dt (utils/get-today-date)]
    (while (and (run dt)
                (<= (Integer. (utils/get-hour)) 20))
      (log/info "run one time")
      (Thread/sleep 600000))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (start-app args))

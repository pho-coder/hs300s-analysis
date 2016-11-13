(ns hs300s-analysis.analysis
  (:require [incanter.core :as incanter]

            [hs300s-analysis.config :refer [trans-type]]
            [hs300s-analysis.utils :as utils]))

(defn analysis-one [data & {:keys [big-amount big-volume start-time end-time]
                            :or {big-amount 0
                                 big-volume 0
                                 start-time "09:00:00"
                                 end-time "15:30:00"}}]
  (let [cleaned-data-volume (incanter/add-derived-column :volume-int [:volume] #(Integer. %) data)
        cleaned-data (incanter/add-derived-column :amount-int [:amount] #(Integer. %) cleaned-data-volume)
        time-filtered-data (incanter/$where {:time {:$fn (fn [t]
                                                           (and (>= (compare t start-time)
                                                                    0)
                                                                (<= (compare t end-time)
                                                                    0)))}} cleaned-data)
        big-trans-volume (incanter/$where {:volume-int {:$fn (fn [volume]
                                                               (>= volume big-volume))}}
                                          time-filtered-data)
        big-trans (incanter/$where {:amount-int {:$fn (fn [amount]
                                                        (>= amount big-amount))}}
                                   big-trans-volume)
        agg-big-trans (incanter/aggregate [:amount-int] [:type]
                                          :dataset big-trans
                                          :rollup-fun :sum)
        type-data-fn (fn [data type]
                       (incanter/query-dataset data {:type type}))
        type-amount-fn (fn [data tp]
                         (let [type-data (type-data-fn data (tp trans-type))]
                           (if-not (empty? (:rows type-data))
                             (incanter/$ 0 :amount-int type-data)
                             0)))
        big-buy-trans-amount (type-amount-fn agg-big-trans :buy)
        big-sell-trans-amount (type-amount-fn agg-big-trans :sell)
        big-normal-trans-amount (type-amount-fn agg-big-trans :normal)
        type (key (apply max-key val {"buy" big-buy-trans-amount
                                      "sell" big-sell-trans-amount
                                      "normal" big-normal-trans-amount}))]
    {:type type
     :buy-trans-amount big-buy-trans-amount
     :sell-trans-amount big-sell-trans-amount
     :normal-trans-amount big-normal-trans-amount}))

(defn analysis-one-day [path dt & {:keys [big-amount big-volume start-time end-time]
                                   :or {big-amount 0
                                        big-volume 0
                                        start-time "09:00:00"
                                        end-time "15:30:00"}}]
  (let [today-dir (str path "/" dt)
        list-file (str today-dir "/list")
        codes (with-open [r (clojure.java.io/reader list-file)]
                (loop [codes []
                       l (.readLine r)]
                  (if (nil? l)
                    codes
                    (recur (conj codes (first (clojure.string/split l #",")))
                           (.readLine r)))))
        one-day (incanter/to-dataset
                 (loop [codes codes
                        re []]
                   (if (empty? codes)
                     re
                     (let [code (first codes)
                           code-file (str today-dir "/" code ".csv")
                           one (assoc (analysis-one (utils/read-csv code-file)
                                                    :big-amount big-amount
                                                    :big-volume big-volume
                                                    :start-time start-time
                                                    :end-time end-time)
                                      :code code)]
                       (recur (rest codes)
                              (conj re one))))))
        agg-trans (incanter/aggregate [:buy-trans-amount
                                       :sell-trans-amount
                                       :normal-trans-amount]
                                      :type
                                      :dataset one-day
                                      :rollup-fun :sum)
        agg-count (incanter/aggregate [:count]
                                      :type
                                      :dataset one-day
                                      :rollup-fun :count)
        summary (incanter/$join [:type :type] agg-trans agg-count)]
    summary))

(defn summary-one-day [path dt]
  (let [all (analysis-one-day path dt)
        afternoon (analysis-one-day path dt :start-time "12:30:00")
        volume-400 (analysis-one-day path dt :big-volume 400)
        amount-10 (analysis-one-day path dt :big-amount 100000)]
    {:all all
     :afternoon afternoon
     :volume-400 volume-400
     :amount-10 amount-10}))

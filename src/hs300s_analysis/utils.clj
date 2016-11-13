(ns hs300s-analysis.utils
  (:import (java.io FileReader)
           (com.opencsv CSVReader))
  (:require [incanter.core :as incanter]
            [clojure.data.csv :as csv]))

(defn read-csv [path]
  (let [parse-line-fn (fn [line]
                        (vec line))
        data (with-open [reader ^CSVReader (CSVReader. (clojure.java.io/reader path))]
               (loop [lines []]
                     (if-let [line (.readNext reader)]
                       (recur (conj lines (parse-line-fn line)))
                       lines)))
        header (map keyword (first data))]
    (incanter/dataset header (rest data))))

(defn write-csv [data path]
  (with-open [f-out (clojure.java.io/writer path)]
    (csv/write-csv f-out [(map name (incanter/col-names data))])
    (csv/write-csv f-out (incanter/to-list data))))

(defn get-today-date
  []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (java.util.Date.)))

(defn get-hour
  []
  (.format (java.text.SimpleDateFormat. "HH") (java.util.Date.)))

(defn check-today-data
  [path])

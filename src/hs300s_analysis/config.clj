(ns hs300s-analysis.config
  (:require [mount.core :refer [defstate]]
            [cprop.source :as source]))

(defstate trans-type :start {:buy "买盘"
                             :sell "卖盘"
                             :normal "中性盘"})

(defstate env :start (source/from-env))

(ns clojure.test.check.impl)

(defn get-current-time-millis []
  #?(:clj  (System/currentTimeMillis)
     :cljr (Environment/TickCount)))

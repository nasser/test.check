(ns ^{:author "Gary Fredericks"
      :doc "A protocol and helper functions for trial results."}
    clojure.test.check.results)

(defprotocol Result
  (pass? [result] "A boolean indicating if the result passed.")
  (result-data [result] "A map of data about the trial."))

(extend-protocol Result
  #?(:clj Object :cljr Object)
  (pass? [this] (boolean this))
  (result-data [_] nil)

  nil
  (pass? [_] false)
  (result-data [_] nil))

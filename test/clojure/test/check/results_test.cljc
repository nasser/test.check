(ns clojure.test.check.results-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.results :as results]))

(deftest default-passing-values
  (is (not (results/pass? nil)))
  (is (not (results/pass? false)))
  (are [x] (results/pass? x)
    :keyword
    'symbol
    "string"
    []
    {}
    #{}
    ()
    42
    42.0
    true))

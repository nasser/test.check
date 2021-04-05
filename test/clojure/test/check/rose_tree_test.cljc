(ns clojure.test.check.rose-tree-test
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.rose-tree :as rose]
            [clojure.test.check.clojure-test :as ct :refer [defspec]]))

(defn depth-one-children
  [rose]
  (into [] (map rose/root (rose/children rose))))

(defn depth-one-and-two-children
  [rose]
  (let [the-children (rose/children rose)]
    (into []
          (concat
           (map rose/root the-children)
           (map rose/root (mapcat rose/children the-children))))))

(defspec test-collapse-rose
  100
  (prop/for-all [i gen/small-integer]
                (let [tree (#'gen/int-rose-tree i)]
                  (= (depth-one-and-two-children tree)
                     (depth-one-children (rose/collapse tree))))))

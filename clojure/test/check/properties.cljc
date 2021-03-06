(ns clojure.test.check.properties
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.results :as results]))

(defrecord ErrorResult [error]
  results/Result
  (pass? [_] false)
  (result-data [_]
    ;; spelling out the whole keyword here since `::error` is
    {:clojure.test.check.properties/error error}))

(defn ^:private exception?
  [x]
  (instance? #?(:clj Throwable :cljr Exception) x))

(defn ^:private apply-gen
  [function]
  (fn [args]
    (let [result (try
                   (let [ret (apply function args)]
                     ;; TCHECK-131: for backwards compatibility (mainly
                     ;; for spec), treat returned exceptions like thrown
                     ;; exceptions
                     (if (exception? ret)
                       (throw ret)
                       ret))
                   #?(:clj (catch java.lang.ThreadDeath t (throw t)))
                   (catch #?(:clj Throwable :cljr Exception) ex
                     (->ErrorResult ex)))]
      {:result result
       :function function
       :args args})))

(defn for-all*
  "A function version of `for-all`. Takes a sequence of N generators
  and a function of N args, and returns a property that calls the
  function with generated values and tests the return value for
  truthiness, like with `for-all`.
  Example:
  (for-all* [gen/large-integer gen/large-integer]
            (fn [a b] (>= (+ a b) a)))"
  [args function]
  (gen/fmap
   (apply-gen function)
   (apply gen/tuple args)))

(defn- binding-vars
  [bindings]
  (map first (partition 2 bindings)))

(defn- binding-gens
  [bindings]
  (map second (partition 2 bindings)))

(defmacro for-all
  "Returns a property, which is the combination of some generators and
  an assertion that should be true for all generated values. Properties
  can be used with `quick-check` or `defspec`.
  `for-all` takes a `let`-style bindings vector, where the right-hand
  side of each binding is a generator.
  The body should be an expression of the generated values that will
  be tested for truthiness, unless it is a special implementation of
  the clojure.test.check.results/Result protocol. Exceptions in the
  body will be caught and treated as failures.
  When there are multiple binding pairs, the earlier pairs are not
  visible to the later pairs.
  If there are multiple body expressions, all but the last one are
  executed for side effects, as with `do`.
  Example:
  (for-all [a gen/large-integer
            b gen/large-integer]
    (>= (+ a b) a))"
  [bindings & body]
  `(for-all* ~(vec (binding-gens bindings))
             (fn [~@(binding-vars bindings)]
               ~@body)))

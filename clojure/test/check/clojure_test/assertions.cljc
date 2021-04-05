(ns clojure.test.check.clojure-test.assertions
  (:require [clojure.test :as t]))

#?(:clj
   (defn test-context-stacktrace [st]
     (drop-while
      #(let [class-name (.getClassName ^StackTraceElement %)]
         (or (.startsWith class-name "java.lang")
             (.startsWith class-name "clojure.test$")
             (.startsWith class-name "clojure.test.check.clojure_test$")
             (.startsWith class-name "clojure.test.check.clojure_test.assertions")))
      st))
   :cljr
   (defn test-context-stacktrace [st]
     (drop-while
      #(let [class-name (.FullName (.GetType ^System.Diagnostics.StackFrame %))]
         (or (.StartsWith class-name "System")
             (.StartsWith  class-name "clojure.test+")
             (.StartsWith  class-name "clojure.test.check.clojure_test+")
             (.StartsWith  class-name "clojure.test.check.clojure_test.assertions")))
      st)))

#?(:clj
   (defn file-and-line*
     [stacktrace]
     (if (seq stacktrace)
       (let [^StackTraceElement s (first stacktrace)]
         {:file (.getFileName s) :line (.getLineNumber s)})
       {:file nil :line nil}))
   :cljr
   (defn file-and-line*
     [stacktrace]
     (if (seq stacktrace)
       (let [^System.Diagnostics.StackFrame s (first stacktrace)]
         {:file (.GetFileName s) :line (.GetFileLineNumber s)})
       {:file nil :line nil})))

(defn check-results [m]
  (if (:pass? m)
    (t/do-report
     {:type :pass
      :message (dissoc m :result)})
    (t/do-report
     (merge {:type :fail
             :expected {:result true}
             :actual m}
            #?(:clj
               (file-and-line* (test-context-stacktrace (.getStackTrace (Thread/currentThread))))
               :cljr
               (file-and-line* (test-context-stacktrace (.GetFrames (System.Diagnostics.StackTrace.)))))))))

(defn check?
  [_ form]
  `(let [m# ~(nth form 1)]
     (check-results m#)))


(defmethod t/assert-expr 'clojure.test.check.clojure-test/check?
  [_ form]
  (check? _ form))

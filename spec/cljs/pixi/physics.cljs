(ns example.test
    (:require [specljs.core :as speclj])
    (:require-macros [specljs.core :refer [describe it should should-not]]))

(defn true-or-false []
  true)

(describe "truthiness"
  (it "tests if true-or-false returns true"
    (should (true-or-false)))

  (it "failure"
    (should (true-or-false))))

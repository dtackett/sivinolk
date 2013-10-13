(ns clinp.test
  (:require [clinp.core :as clinp]))

(defn run []
    (assert (= (+ 2 2) 4))
  (assert (= (clinp/get-key-code "ruff") nil)))

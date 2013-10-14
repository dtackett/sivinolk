(ns clinp.test
  (:require [clinp.core :as clinp])
  (:require-macros [specljs.core :refer [describe it should should-not should= should-be-nil]]))

(describe "Key code lookups"
          (it "Getting unknown keycode should return nil"
              (should-be-nil (clinp/get-key-code "unknowablekey"))))

(ns pixi-cljs.scratch)

; Test code for watches
(defn run-atom-testing []
  (let [atom-test (atom #{})]
    (add-watch atom-test "test2" (fn [key ref old new] (.log js/console "Watched 2")))
    (swap! atom-test conj "test")
    ))


; (run-atom-testing)

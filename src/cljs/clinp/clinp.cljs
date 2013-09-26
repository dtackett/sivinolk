(ns clinp.core
  (:require [clojure.browser.event :as event]))

; should follow the format of a map of key-code to a map of phases to a callback
(def listeners (atom #{}))

(def downkeys (atom #{}))

(def keymap {:left-arrow 37
             :up-arrow 38
             :right-arrow 39
             :down-arrow 40})

(defn- exec [key-code phase]
  "Execute the function (if found) for the given bind key and phase"
  (get (get listeners key-code) phase))

(defn setup []
  "Perform initial setup for input handler"
  ; setup the google handler
  (reset! downkeys #{}))

(defn teardown []
  "Perform any cleanup needed for input handler"
  ; remove the google handler
  ; todo remove all current handlers
  (reset! downkeys #{}))

(defn keydown? [test-key]
  "Test if the given key is currently held down."
  (contains? @downkeys (get keymap test-key)))

(event/listen
	(.-body js/document)
	"keydown"
  (fn [event]
    (do
; Beginning work on dispatching an initial event when the key is first pressed
;      (if (not (contains? @downkeys (.-keyCode event)))
;        (.log js/console (str "Keydown " (.-keyCode event))))
      (swap! downkeys conj (.-keyCode event)))))

(event/listen
	(.-body js/document)
	"keyup"
  (fn [event]
    (swap! downkeys disj (.-keyCode event))))

(defn listen [bind-key phase f]
  "Setup an callback for a given key and phase."
  (swap! listeners assoc (get keymap bind-key) (assoc (get listeners (get keymap bind-key) #{}) phase f)))

(defn unlisten [bind-key phase]
  "Remove a callback for a given key and phase.")

(defn pulse []
  "Trigger the pulse phase.")

(def testmap {:b "1" :C "2"})
(get testmap :C)

(get :down-arrow keymap)

(listen :down-arrow :down (fn [] (.log js/console "Down Pressed")))

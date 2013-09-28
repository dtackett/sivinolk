(ns clinp.core
  (:require [clojure.browser.event :as event]
            [goog.events.KeyCodes]))

; Thoughts: Is this just going to end up in a handler soup?
; Would this be better modeled by having the pulse generate events that go
; to some queue and allow something else to consume from that queue? That
; queue would have to be rebuilt every pulse? That might not be too bad if
; you assume that from pulse to pulse the down keys don't change that much.

; TODO tests
; test get-key-code for known key
; test get-key-code for unknown key
; test exec on non registered key handler
; test exec on non registered phase
; test exec on a register key handler and phase
; keydown test false
; keydown test true
; setup
; teardown
; listen
; listen same key, new phase
; listen bad key
; listen bad phase
; listen overwrite
; unlisten
; unlisten bad key
; unlisten bad phase
; unlisten non-registered

; should follow the format of a map of key-code to a map of phases to a callback
(def listeners (atom {}))

(def downkeys (atom #{}))

(defn- get-key-code [key-name]
  "Find the keyCode integer value for a keyword"
  (aget js/goog.events.KeyCodes (name key-name)))

(defn- exec-handler [phase key-code]
  "Execute the function (if found) for the given bind key and phase"
  (let [handler (get (get @listeners key-code) phase)]
    (if (not (nil? handler))
      (handler))))

; TODO Allow for an event listener target? Currently hard set to be the document body
(defn setup []
  "Perform initial setup for input handler"
  (do
    (reset! downkeys #{})
    ; Setup keydown handler
    (event/listen
      (.-body js/document)
      "keydown"
      (fn [event]
        (do
          (if (not (contains? @downkeys (.-keyCode event)))
            (exec-handler :down (.-keyCode event)))
          (swap! downkeys conj (.-keyCode event)))))
    ; Setup keyup handler
    (event/listen
      (.-body js/document)
      "keyup"
      (fn [event]
          (if (contains? @downkeys (.-keyCode event))
            (exec-handler :up (.-keyCode event)))
        (swap! downkeys disj (.-keyCode event))))))

(defn teardown []
  "Perform any cleanup needed for input handler"
  ; remove the google handler
  ; todo remove all current handlers
  (reset! downkeys #{}))

(defn keydown? [test-key]
  "Test if the given key is currently held down."
  (contains? @downkeys (get-key-code test-key)))

(defn listen [bind-key phase f]
  "Setup an callback for a given key and phase."
  ; TODO How to validate phase is in a known set?
  ; TODO How to validate bind-key is known?
  (let [key-code (get-key-code bind-key)]
    (swap!
     listeners
     assoc
     key-code
     (assoc
       (get @listeners key-code {})
       phase
       f))))

; TODO Implement this function
(defn unlisten [bind-key phase]
  "Remove a callback for a given key and phase.")

(defn pulse []
  "Trigger the pulse phase."
  (dorun (map (partial exec-handler :pulse) @downkeys)))

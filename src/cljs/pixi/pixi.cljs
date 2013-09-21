(ns pixi-cljs.core
  (:require [clojure.browser.event :as event]))

;; setup renderer
(def renderer (js/PIXI.autoDetectRenderer 400 300))

(.appendChild (.-body js/document) (.-view renderer))

;; setup stage
(def stage (js/PIXI.Stage. 0x66ff99))

(def bunny-texture (js/PIXI.Texture.fromImage "images/bunny.png"))

;; some helper functions for setting up sprites

;; I feel like set-position and set-anchor could be made easier
;; if I had a grasp of macros
(defn set-position [sprite x y]
  (set! (.-position.x sprite) x)
  (set! (.-position.y sprite) y))

(defn set-anchor [sprite x y]
  (set! (.-anchor.x sprite) x)
  (set! (.-anchor.y sprite) y))

;; This is flawed as it takes a potentially unchanging input but expects
;; the side effects to be re-evaluated every time.
(defn create-simple [texture]
  (def sprite (js/PIXI.Sprite. texture))
  (set-position sprite 200 100)
  (set-anchor sprite 0.5 0.5)
  (. stage addChild sprite)
  sprite)

(defn rotate [entity delta]
  "Change the rotate by the given delta"
  (let [sprite (:sprite entity)]
    (set! (.-rotation (:sprite entity)) (+ delta (.-rotation (:sprite entity))))))

(defn move [entity dx dy]
  "Move by the given change to x and y"
  (let [sprite (:sprite entity)]
    (set-position sprite
                  (+ dx (.-position.x sprite))
                  (+ dy (.-position.y sprite)))))

(defrecord Entity [sprite])

(defn make-entity [stage texture]
  (let [sprite (js/PIXI.Sprite. texture)]
    (set-position sprite 100 50)
    (set-anchor sprite 0.5 0.5)
    (. stage addChild sprite)
  (Entity. sprite)))


(def my-bunny (make-entity stage bunny-texture))
(def bunny (make-entity stage bunny-texture))

;; function to remove all elements from the stage
(defn empty-stage [stage]
  (doall (map
   (fn [target]
    ;; (.log js/console target)
     (. stage removeChild target)
     )
   ;; do a slice 0 here to copy the array as the stage array mutates with removes
   (.slice (.-children stage) 0))))

;(empty-stage stage)

;(create-simple bunny-texture)

;(def my-bunny (create-simple bunny-texture))
;(def bunny (create-simple bunny-texture))

;; Hacky attempt at key handling
(def downkeys #{})

(event/listen
	(.-body js/document)
	"keydown"
  (fn [event]
    (def downkeys (conj downkeys (.-keyCode event)))))

(event/listen
	(.-body js/document)
	"keyup"
  (fn [event]
    (def downkeys (disj downkeys (.-keyCode event)))))

(defn check-input []
  (when (contains? downkeys 38)
    (move my-bunny 0 -1))
  (when (contains? downkeys 37)
    (move my-bunny -1 0))
  (when (contains? downkeys 39)
    (move my-bunny 1 0))
  (when (contains? downkeys 40)
    (move my-bunny 0 1)))


;; update world function
(defn update-world[]
  (do
    (check-input)
    (rotate bunny 0.2)
;  (move my-bunny 1 0)
;  (set! (.-position.x bunny) (+ 1 (.-position.x bunny)))
  ))

;; setup animation loop
(defn animate[]
  (js/requestAnimFrame animate)
  (update-world)
  (. renderer render stage))

(js/requestAnimFrame animate)

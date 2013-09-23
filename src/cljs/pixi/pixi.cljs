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

; Deprecated
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

;; Create a main record to define the world
; TODO: Stage should be provided and this whole thing should be built with a function
(def world (atom {:next-id 0 :entities {} :stage stage}))
;; World should have a list of all entities in the world
;; World should be attached to a stage?
;; Each entity should have some unique id
;; Create a function to find an entity based on id in a world
(defn get-entity [world entity-id]
  (get (:entities world) entity-id))
;; Create a function to add an entity to the world
(defn add-entity [world entity]
  (let [entity-id (:next-id world)
        entities (assoc (:entities world) entity-id entity)]
      (assoc world
        :next-id (inc entity-id)
        :entities entities)))
;; Create a function to remove an entity from the world

(swap! world (fn [] (add-entity @world bunny)))
(swap! world (fn [] (add-entity @world my-bunny)))

;; function to remove all elements from the stage
(defn empty-stage [stage]
  "Remove all Pixi DisplayObjects from the given stage."
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
(def downkeys (atom #{}))

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

; Hack to control which entity we are moving
(def target-entity 0)

; TODO: The function to execute in response to the key being pressed should
; not be hard coded.
(defn check-input [keystate]
  "Intended to be the main loop to check for user input."
  (when (contains? keystate 38)
    (move (get-entity @world target-entity) 0 -1))
  (when (contains? keystate 37)
    (move (get-entity @world target-entity) -1 0))
  (when (contains? keystate 39)
    (move (get-entity @world target-entity) 1 0))
  (when (contains? keystate 40)
    (move (get-entity @world target-entity) 0 1)))


;; update world function
(defn update-world[]
  "Main game update function. Everything but rendering would fall in here."
  (do
    (check-input @downkeys)
    (if (get-entity @world 0)
      (rotate (get-entity @world 0) 0.2))
;  (move my-bunny 1 0)
;  (set! (.-position.x bunny) (+ 1 (.-position.x bunny)))
  ))

;; setup animation loop
(defn animate[]
  "Core callback loop."
  (js/requestAnimFrame animate)
  (update-world)
  (. renderer render stage))

(js/requestAnimFrame animate)

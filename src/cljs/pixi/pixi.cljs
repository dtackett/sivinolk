(ns pixi-cljs.core
  (:require [clojure.browser.event :as event]
            [clinp.core :as clinp]
            [pixi.components :as components])
  (:require-macros [pixi.macro :refer [component]]))

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


; Components
; (Some components are dependent on the existance of others)
; position component [x, y]
; collision component [aabb]
; physics component [vx, vy, ax, ay]
; render component [sprite]

(component pixi-renderer [sprite])
(component position [x y])
(component rotation [r])
(component velocity [x, y])

; Soon to be deprecated?
(defrecord Entity [sprite])

(defn- add-component [e c]
  "Add a component by its name to the given map"
  (assoc e (components/component-name c) c))

(defn compose-entity [components]
  (reduce add-component {} components))

;(defn get-component [entity component]
;  (loop [components entity]
;    (cond (empty? components) nil
;          (instance? component (first components)) (first components)
;          :else (recur (rest components)))))

(def test-entity (compose-entity
                    [(pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                     (rotation. 0)
                     (position. 100 100)]))

(:position test-entity)

;(component-name (get-component test-entity position))

;(defn compound [coll c]
;  (assoc coll (component-name c) c))

;(compound {} (get-component test-entity position))

;(reduce compound {} test-entity)

;(Entity. [(position 10 10)
;          (velocity 1 0)
;          (pixi-renderer bunny-texture)])
; Ok smart guy. You've created an entity and added all these components.
; How do you know what components an entity has?

; (-> myEntity has-component? :component)
; (-> myEntity get-component :component)
; (-> myEntity update-component new-component-value)
; (-> world update-entity 1 )
(defn update-entity [world new-entity]
  "Generate a new world state from the given updated entity state."
  (assoc
    world
    :entities
    (assoc
      (:entities world)
      (:entity-id new-entity)
      new-entity)))

; Entities
; [the entities in our system]

; Systems
; physics system
;  run through and move all position entries by the
; pixi-render system
;  run through and setup the sprites based on current x,y data

; If we make the presumption that there will be a single identifying component for
; every system we can create a simple function to loop through the entities with
; the requested component.

; Game loop runs through all the systems
; QUESTION: how do we handle events in this? What do we do when two entities hit?

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

;; This is probably unsafe anymore, should be replaced or used in conjunction with
;; a function that will clear up the entities in the game world as well.
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

; Hack to control which entity we are moving
(def target-entity (atom 0))

; clinp setup and keyboard handlers
(clinp/setup!)

(clinp/listen! :Z :down
              (fn [] (swap! target-entity
                            (fn [cur]
                              (if (>= (inc cur) (:next-id @world))
                                0
                                (inc cur))))))

(clinp/listen! :X :down
              (fn [] (swap! world
                            (fn [] (add-entity @world (make-entity stage bunny-texture))))))

(clinp/listen! :UP :pulse
              (fn [] (move (get-entity @world @target-entity) 0 -1)))

(clinp/listen! :DOWN :pulse
              (fn [] (move (get-entity @world @target-entity) 0 1)))

(clinp/listen! :LEFT :pulse
              (fn [] (move (get-entity @world @target-entity) -1 0)))

(clinp/listen! :RIGHT :pulse
              (fn [] (move (get-entity @world @target-entity) 1 0)))


;; update world function
(defn update-world[]
  "Main game update function. Everything but rendering would fall in here."
  (do
    (clinp/pulse!)
    (if (get-entity @world 0)
      (rotate (get-entity @world 0) 0.2))

;    (rotate (get-entity @world 5) 0.01)
  ))

;; setup animation loop
(defn animate[]
  "Core callback loop."
  (js/requestAnimFrame animate)
  (update-world)
  (. renderer render stage))

(js/requestAnimFrame animate)

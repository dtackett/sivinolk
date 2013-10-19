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

(def ugly-block-texture (js/PIXI.Texture.fromImage "images/ugly-block.png"))

;; some helper functions for setting up sprites

;; I feel like set-position and set-anchor could be made easier
;; if I had a grasp of macros
(defn set-position [sprite x y]
  (set! (.-position.x sprite) x)
  (set! (.-position.y sprite) y))

(defn set-anchor [sprite x y]
  (set! (.-anchor.x sprite) x)
  (set! (.-anchor.y sprite) y))

; TODO Implement some system to replace this functionality
(defn rotate [entity delta]
  "Change the rotate by the given delta"
  (let [sprite (:sprite entity)]
    (set! (.-rotation (:sprite entity)) (+ delta (.-rotation (:sprite entity))))))


; Components
; (Some components are dependent on the existance of others)
; position component [x, y]
; collision component [aabb]
; physics component [vx, vy, ax, ay]
; render component [sprite]

(component pixi-renderer [sprite])
(component position [x y])
(component rotation [r]) ; Currently nothing pays attention to the rotation
(component velocity [x y])
(component aabb [w h])      ; axis aligned bounding box
(component controllable []) ; Whether the entity can be controlled
(component id [id])

; Entity composition functions
(defn- add-component [e c]
  "Add a component by its name to the given map"
  (assoc e (components/component-name c) c))

(defn compose-entity [components]
  (reduce add-component {} components))

; Pixi system functions
(defn- ensure-entity-on-stage! [stage entity]
  "Ensure the entity is on the given stage"
  (let [sprite (:sprite (:pixi-renderer entity))]
    (if (nil? (.-stage sprite))
      (. stage addChild sprite))))

(defn update-display [entity]
  "Update the pixi-renderer component with the current state"
  (let [pos-comp (:position entity)
        sprite (:sprite (:pixi-renderer entity))]
    (set-position sprite (:x pos-comp) (:y pos-comp))))

(defn pixi-setup-entity [stage entity]
  (if (:pixi-renderer entity)
    (do
      (ensure-entity-on-stage! stage entity)
      (update-display entity))))



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


;; World should have a list of all entities in the world
;; World should be attached to a stage?
;; Each entity should have some unique id
;; Create a function to find an entity based on id in a world
(defn get-entity [world entity-id]
  (get (:entities world) entity-id))

;; Create a function to add an entity to the world
(defn add-entity [world entity]
  (let [entity-id (:next-id world)
        ; Add an id component to the entity
        entity (add-component entity (id. entity-id))
        entities (assoc (:entities world) entity-id entity)]
      (assoc world
        :next-id (inc entity-id)
        :entities entities)))

; TODO This should blow up gracefully if the entity does not have an id component
(defn update-entity [world entity]
  "Generate a new world state from the given updated entity state."
  (assoc
    world
    :entities
    (assoc
      (:entities world)
      (-> entity :id :id)
      entity)))




;; TODO Create a function to remove an entity from the world


(defn simple-add-entity! [world]
  (swap! world #(add-entity @world (compose-entity
                        [(pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                         (rotation. 0)
                         (velocity. 0 3)
                         (aabb. 26 37)
                         (controllable.)
                         (position. 100 100)]))))

;; Create a main record to define the world
; TODO: Stage should be provided and this whole thing should be built with a function

; world-state works on the new entity system
(def world-state (atom {:next-id 0 :entities {} :stage stage}))

;; Add a simple world block
(swap! world-state (fn [] (add-entity @world-state (compose-entity
                        [(pixi-renderer. (js/PIXI.Sprite. ugly-block-texture))
                         (position. 100 190)
                         (aabb. 16 16)]))))

(swap! world-state (fn [] (add-entity @world-state (compose-entity
                        [(pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                         (rotation. 0)
                         (velocity. 0 3)
                         (aabb. 26 37)
                         (controllable.)
                         (position. 100 100)]))))

(swap! world-state (fn [] (add-entity @world-state (compose-entity
                        [(pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                         (rotation. 0)
                         (velocity. 0 3)
                         (controllable.)
                         (position. 100 100)]))))

(swap! world-state (fn [] (add-entity @world-state (compose-entity
                        [(pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                         (rotation. 0)
                         (velocity. 10 4)
                         (controllable.)
                         (position. 100 100)]))))

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

; pixi render system
(defn render-system [world]
  (do
    (dorun
     (map
      (partial pixi-setup-entity (:stage world))
      (vals (:entities world))))
    (. renderer render (:stage world))))

; physics related stumblings
(defn move [entity x y]
  "Move the entity by the given x and y"
  (let [position (:position entity)]
      (add-component
       entity
       (merge position {:x (+ x (:x position))
                        :y (+ y (:y position))}))))

(defn simple-input-move! [world target-id x y]
  (let [entity (get-entity @world target-id)]
      (swap! world #(update-entity % (move entity x y)))))

(defn apply-velocity-to-entity [entity]
  "Apply velocity to the given entity"
  (let [velocity (:velocity entity)]
    (move entity (:x velocity) (:y velocity))))

(def world-bound {:x 250 :y 250})

(defn HACK-check-bound [keyname position world-bound]
  (let [k (keyword keyname)]
    (cond (> (k position) (k world-bound))
            (k world-bound)
          (< (k position) 0)
            0
          :default (k position))))

(defn HACK-force-world-bounds [entity]
  (let [position (:position entity)]
      ; reset position to world bound
      ; TOTHINK how to handle velocity change?
      (add-component
       entity
       (merge position {:x (HACK-check-bound "x" position world-bound)
                        :y (HACK-check-bound "y" position world-bound)}))))

;(HACK-check-bound "x" (position. 110 10) {:x 100 :y 100})

(defn get-bounds [entity]
  "Get the world bounds of the given entity. Returns nil if the entity does not have the appropriate components."
  (let [position (:position entity)
        aabb (:aabb entity)]
    (if (and position
             aabb)
      {:l (:x position)
       :t (:y position)
       :r (+ (:x position) (:w aabb))
       :b (+ (:y position) (:h aabb))})))

(defn get-midpoint [entity]
  "Get the midpoint of the given entity. Based on position and aabb."
  (let [position (:position entity)
        aabb (:aabb entity)]
    (if (and position aabb)
      {:x (+ (:x position) (/ (:w aabb) 2))
       :y (+ (:y position) (/ (:h aabb) 2))})))

(defn collision? [ea eb]
  "Check if the two given entities are in collision"
  (if (= (-> ea :id :id) (-> eb :id :id))
    false
    (let [collider (get-bounds ea)
          collidee (get-bounds eb)]
      (if (and collider collidee)
        (not
         (or (< (:b collider) (:t collidee))
             (< (:r collider) (:l collidee))
             (> (:l collider) (:r collidee))
             (> (:t collider) (:b collidee))))
        false))))

(defn resolve-collision [ea eb]
  "Crude collision resolver. Assumes that a collision has already been verified."
  (let [mida (get-midpoint ea)
        midb (get-midpoint eb)
        dx (- (:x mida) (:x midb))
        dy (- (:y mida) (:y midb))]
    (if
      (> (.abs js/Math dx) (.abs js/Math dy))
      (if (> dx 0)
        ; approach is on the horizontal
        (add-component ea (merge (:position ea) {:x (+ (:x (:position eb)) (:w (:aabb eb)))}))
        (add-component ea (merge (:position ea) {:x (- (:x (:position eb)) (:w (:aabb ea)))})))
      (if (> dy 0)
        ; approach is on the vertical
        (add-component ea (merge (:position ea) {:y (+ (:y (:position eb)) (:h (:aabb eb)))}))
        (add-component ea (merge (:position ea) {:y (- (:y (:position eb)) (:h (:aabb ea)))})))
      )))

(defn resolve-collisions [entity world]
  (reduce
   (fn [entity test-entity]
     (if (collision? entity test-entity)
       (resolve-collision entity test-entity)
       entity))
   entity
   (vals (:entities world))
   ))

(defn do-physics-simulation [entity world]
  "Apply physics simulation to the given entity"
  ; apply velocity
  (if (:velocity entity)
    (-> entity
        apply-velocity-to-entity
        (resolve-collisions world)
        HACK-force-world-bounds))
  ; TODO check for collisions
  ; TODO resolve collisions
  ; TOTHINK what to do with collision events?
  )

; physics system
(defn physics-system [world]
  (reduce
   (fn [world entity] (update-entity world (do-physics-simulation entity world)))
    world
    (vals (:entities world))))

(defn update-world! [world]
               (clinp/pulse!)
               (swap! world #(physics-system %)))

; clinp setup and keyboard handlers
(clinp/setup!)

(defn- select-next-controllable [world current-target]
  "Select the next entity that has a controllable component. This is not smart enough to avoid infinite loops."
  (loop [t (inc current-target)]
    (let [newt (if (>= t (:next-id world)) 0 t)]
      (if (:controllable (get-entity world newt))
        newt
        (recur (inc newt))))))

; Hack to control which entity we are moving
(def target-entity (atom (select-next-controllable @world-state 0)))

(clinp/listen! :Z :down
              (fn [] (swap! target-entity #(select-next-controllable @world-state %))))

(clinp/listen! :X :down
               #((simple-add-entity! world-state)
                 (swap! target-entity (fn [] (dec (:next-id @world-state))))))

(clinp/listen! :UP :pulse
               #(simple-input-move! world-state @target-entity 0 -6))

;(clinp/listen! :DOWN :pulse
;               #(simple-input-move! world-state @target-entity 0 2))

(clinp/listen! :LEFT :pulse
               #(simple-input-move! world-state @target-entity -3 0))

(clinp/listen! :RIGHT :pulse
               #(simple-input-move! world-state @target-entity 3 0))

;; setup animation loop
(defn animate[]
  "Core callback loop."
  (js/requestAnimFrame animate)
  (update-world! world-state)
  (render-system @world-state)
  (. renderer render stage))

(js/requestAnimFrame animate)

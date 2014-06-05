(ns sivinolk.core
  (:require [clinp.core :as clinp]
            [sivinolk.pixi :as pixi]
            [sivinolk.physics :as physics]
            [sivinolk.components :as comps]
            [vyrlynd.entity :as entity]
            [vyrlynd.world :as world]))

;; Need to divorce world state from game state
;; game state can keep a list of the previous world states

;; Create a main record to define the world
(def world-state (atom world/base-world))
(def saved-world (atom @world-state))

;; Build a queue for the inputs. This should probably be within the clinp library itself.
(def input-queue (atom []))

; Load some textures for entities
(def alien-texture (js/PIXI.Texture.fromImage "images/alien.png"))
(def ugly-block-texture (js/PIXI.Texture.fromImage "images/ugly-block.png"))

; Hack to control which entity we are moving (this really should be part of the world state?)
(def target-entity (atom 0))
@target-entity

(defn select-next-controllable
  "Select the next entity that has a controllable component. This is not smart enough to avoid infinite loops."
  [world current-target]
  (loop [t (inc current-target)]
    (let [newt (if (>= t (:next-id world)) 0 t)]
      (if (:controllable (world/get-entity world newt))
        newt
        (recur (inc newt))))))

(defn select-entity
  "Select the given entity"
  [world entity]
  (swap! target-entity #((:id (:id entity)))))

(def text-entity (entity/compose-entity
                  [(comps/pixi-renderer. (js/PIXI.Text. "Hello World"))
                   (comps/position. 10 10)]))

(#_(let [resp (world/add-entity @world-state text-entity)]
     (do
       (def text-entity (:entity resp))
       (swap! world-state #(:world resp)))))

(defn update-text-display! [text]
  (#_(.setText (-> text-entity :pixi-renderer :sprite) text)))

;; It would be handy to create an entity template?
;; Updating the entity would be simple?
(defn add-block
  [world x y]
  (:world (world/add-entity world (entity/compose-entity
                                   [(comps/pixi-renderer. ugly-block-texture)
                                    (comps/position. x y)
                                    (comps/aabb. 16 16)]))))

;; This should be moved toward function purity.
;; Reliant on too many things outside of it.
;; Consider the idea of entity templates
(defn load-sample-world! []
  (do
    (swap! world-state
           (fn [world]
             (:world (world/add-entity world
                                       (entity/compose-entity [(comps/viewport. 20 0)
                                                               (comps/world-bounds. 500 250)])))))
    ;; Add some simple blocks
    (swap! world-state (fn [world] (-> world
                                       (add-block 100 190)
                                       (add-block 116 190)
                                       (add-block 500 190)
                                       (add-block 500 100))))

    ; Add some test aliens
    (swap! world-state (fn [world] (:world (world/add-entity world (entity/compose-entity
                                                                      [(comps/pixi-renderer. alien-texture)
                                                                       (comps/rotation. 0)
                                                                       (comps/velocity. 0 3)
                                                                       (comps/aabb. 17 21)
                                                                       (comps/controllable. 0 true)
                                                                       (comps/position. 100 100)])))))

    (swap! target-entity #(select-next-controllable @world-state 0))
    ))

;; Scratch functions for playing with user input
(defn simple-input-move [world target-id x y]
  (let [entity (world/get-entity world target-id)]
    (world/update-entity world (physics/move entity x y))))

(defn create-simple-entity
  "Quick utility function to create an alien. It would be better to have some sort of entity templating system. That might be over engineering at this point though."
  []
  (entity/compose-entity [(comps/pixi-renderer. alien-texture)
                                                   (comps/rotation. 0)
                                                   (comps/velocity. 0 3)
                                                   (comps/aabb. 17 21)
                                                   (comps/controllable. 0 true)
                                                   (comps/position. 100 100)]))

(defn simple-add-entity [world]
  (:world (world/add-entity world (create-simple-entity))))

; Setup for the pixi.js system
(defn setup-pixi! [world]
  (swap! world pixi/setup-world!))

(def jump-limit 500)

(defn set-viewport!
  [x y]
  (swap!
   world-state
   (fn [world]
     (world/update-entity
      world
      (let [entity (first (world/get-with-comp world :viewport))]
        (entity/add-component
         entity
         (merge
          (:viewport entity)
          {:x x :y y})))))))

(defn jump-fn [world]
  (let [entity (world/get-entity world @target-entity)
        now (.now js/Date)
        time-diff (- now (:start-jump-time (:controllable entity)))]
    (cond
     (< time-diff jump-limit) (simple-input-move world @target-entity 0 -6)
     :else world)))

;; Hacky input event workings
(defn push-input-event!
  "Push an input event onto the queue"
  [input-key input-event]
  (swap!
   input-queue
   #(conj % [input-key input-event])))

(defn clear-input-queue!
  "Clear the input event queue"
  []
  (swap! input-queue #(vector)))

; ([[in state] test-input & test-world])
(defmulti process-input (fn [input & args] input))

;; Dirty implementation to run all inputs in the queue
#_(defn run-input [world]
  (doall
   (doseq [input @input-queue]
     (process-input input world))
   (clear-input-queue!)))

(defn run-input [world]
  (let [new-world
        (reduce #(process-input %2 %1) world @input-queue)]
    (do
      (clear-input-queue!)
      new-world)))

#_(run-input @world-state)

(defmethod process-input [:Z :down] [in world]
  (do
   (swap! target-entity #(select-next-controllable world %))
   world))

(defmethod process-input [:X :down] [in world]
  ; TODO add entity should be pure
  (let [new-world (simple-add-entity world)]
    (do
      (swap! target-entity (fn [] (dec (:next-id new-world))))
      new-world)))

(defmethod process-input [:UP :down] [in world]
  (world/update-entity world
                       (let [entity (world/get-entity world @target-entity)
                                        controllable (:controllable entity)]
                                    (if (:jump-flag controllable)
                                      (entity/add-component
                                       entity
                                       (merge controllable {:start-jump-time (.now js/Date) :jump-flag false}))))))

(defmethod process-input [:UP :up] [in world]
  (world/update-entity world
                       (let [entity (world/get-entity world @target-entity)
                             controllable (:controllable entity)]
                         (entity/add-component
                          entity
                          (merge controllable {:start-jump-time 0})))))

(defmethod process-input [:UP :pulse] [in world]
  (jump-fn world))

(defmethod process-input [:LEFT :pulse] [in world]
  (simple-input-move world @target-entity -3 0))

(defmethod process-input [:RIGHT :pulse] [in world]
  (simple-input-move world @target-entity 3 0))

(defmethod process-input [:P :down] [in world]
  (do
    (swap! saved-world (fn [world] @world-state))
    world))

(defmethod process-input [:O :down] [in world]
  (do
    (swap! target-entity #(select-next-controllable @saved-world 0))
    @saved-world))

(defn setup-clinp! []
  (do
    ; clinp setup and keyboard handlers
    (clinp/setup!)

    (clinp/listen! :Z :down #(push-input-event! :Z :down))

    (clinp/listen! :X :down #(push-input-event! :X :down))

    (clinp/listen! :UP :down #(push-input-event! :UP :down))

    (clinp/listen! :UP :up #(push-input-event! :UP :up))

    (clinp/listen! :UP :pulse #(push-input-event! :UP :pulse))

    (clinp/listen! :LEFT :pulse #(push-input-event! :LEFT :pulse))

    (clinp/listen! :RIGHT :pulse #(push-input-event! :RIGHT :pulse))

    (clinp/listen! :P :down #(push-input-event! :P :down))

    (clinp/listen! :O :down #(push-input-event! :O :down))
    ))



; World bounds should be pulled from the world
; Updating an entity is extremely kludgy right now.
(defn update-viewport [world]
  (world/update-entity world
                       (let
                         [entity (world/get-entity world @target-entity)
                          viewport (first (world/get-with-comp world :viewport))]
                         (cond (> (- (:x (:position entity)) (:x (:viewport viewport)))300)
                               (entity/update-component viewport :viewport {:x (- (:x (:position entity)) 300)})
                               (< (- (:x (:position entity)) (:x (:viewport viewport)))100)
                               (entity/update-component viewport :viewport {:x (- (:x (:position entity)) 100)})
                               :default
                               viewport
                               )
                         )))

(defn update-world! [world]
  (do
    (clinp/pulse!)
    (swap! world run-input)
    #_(run-input @world)
    (swap! world update-viewport)
    (swap! world physics/physics-system)))

(defn cycle-world [world]
  (do
    (let [start (.now js/Date)]
      (update-world! world)
      (swap! world pixi/render-system)
      (update-text-display! (str (- (.now js/Date) start))))
    ))

;; setup animation loop
(defn animate
  "Core callback loop."
  []
  (js/requestAnimFrame animate)
  (cycle-world world-state))

(defn start []
  (do
    (load-sample-world!)
    (setup-clinp!)
    (setup-pixi! world-state)
    (js/requestAnimFrame animate)
    (set-viewport! 100 10)
    ))

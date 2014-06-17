(ns sivinolk.core
  (:require [clinp.core :as clinp]
            [sivinolk.pixi :as pixi]
            [sivinolk.physics :as physics]
            [sivinolk.components :as comps]
            [vyrlynd.entity :as entity]
            [vyrlynd.world :as world]))

(def game-state (atom {:world world/base-world
                       :saved-world world/base-world
                       :target-entity 0}))

;; Seems like there should be a better name for this.
(defn map-apply [k m f]
  (assoc m k (f (k m))))

(def map-apply-world (partial map-apply :world))

;; Input queue should be from an external system?
;; Build a queue for the inputs. This should probably be within the clinp library itself.
(def input-queue (atom []))

;; These are not ideal but they do not seem best to encapsulate into the game state
; Load some textures for entities
(def alien-texture (js/PIXI.Texture.fromImage "images/alien.png"))
(def ugly-block-texture (js/PIXI.Texture.fromImage "images/ugly-block.png"))


(defn select-next-controllable
  "Select the next entity that has a controllable component. This is not smart enough to avoid infinite loops."
  [world current-target]
  (loop [t (inc current-target)]
    (let [newt (if (>= t (:next-id world)) 0 t)]
      (if (:controllable (world/get-entity world newt))
        newt
        (recur (inc newt))))))

#_(defn select-entity
  "Select the given entity"
  [world entity]
  (swap! target-entity #((:id (:id entity)))))

(def text-entity (entity/compose-entity
                  [(comps/pixi-renderer. (js/PIXI.Text. "Hello World"))
                   (comps/position. 10 10)]))

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
(defn load-sample-world [world]
  (-> world
           ((fn [world]
             (:world (world/add-entity world
                                       (entity/compose-entity [(comps/viewport. 20 0)
                                                               (comps/world-bounds. 500 250)])))))
           (add-block 100 190)
           (add-block 116 190)
           (add-block 500 190)
           (add-block 500 100)

           ; Add some test aliens
           ((fn [world] (:world (world/add-entity world (entity/compose-entity
                                                                      [(comps/pixi-renderer. alien-texture)
                                                                       (comps/rotation. 0)
                                                                       (comps/velocity. 0 3)
                                                                       (comps/aabb. 17 21)
                                                                       (comps/controllable. 0 true)
                                                                       (comps/position. 100 100)])))))))

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
(defn setup-pixi! [game]
  (swap! game #(map-apply-world % pixi/setup-world!)))


(def jump-limit 500)

(defn set-viewport
  [x y world]
     (world/update-entity
      world
      (let [entity (first (world/get-with-comp world :viewport))]
        (entity/add-component
         entity
         (merge
          (:viewport entity)
          {:x x :y y})))))

(defn jump-fn [game]
  (assoc game :world
    (let [world (:world game)
          entity (world/get-entity world (:target-entity game))
          now (.now js/Date)
          time-diff (- now (:start-jump-time (:controllable entity)))]
      (cond
       (< time-diff jump-limit) (simple-input-move world (:target-entity game) 0 -6)
       :else world))))

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

(defn run-input [game]
  (let [new-game
        (reduce #(process-input %2 %1) game @input-queue)]
    (do
      (clear-input-queue!)
      new-game)))

#_(run-input @world-state)

(defmethod process-input [:Z :down] [in game]
  (assoc game :target-entity (select-next-controllable (:world game) (:target-entity game))))

(defmethod process-input [:X :down] [in game]
  ; TODO add entity should be pure
  (let [new-world (simple-add-entity (:world game))]
    (-> game
      (assoc :target-entity (dec (:next-id new-world)))
      (assoc :world new-world))))

(defmethod process-input [:UP :down] [in {world :world :as game}]
  (assoc game :world
    (world/update-entity world
                         (let [entity (world/get-entity world (:target-entity game))
                               controllable (:controllable entity)]
                           (if (:jump-flag controllable)
                             (entity/add-component
                              entity
                              (merge controllable {:start-jump-time (.now js/Date) :jump-flag false})))))))

(defmethod process-input [:UP :up] [in {world :world :as game}]
  (assoc game :world
    (world/update-entity world
                         (let [entity (world/get-entity world (:target-entity game))
                               controllable (:controllable entity)]
                           (entity/add-component
                            entity
                            (merge controllable {:start-jump-time 0}))))))

(defmethod process-input [:UP :pulse] [in {world :world :as game}]
    (jump-fn game))

(defmethod process-input [:LEFT :pulse] [in {world :world :as game}]
  (assoc game :world
    (simple-input-move world (:target-entity game) -3 0)))

(defmethod process-input [:RIGHT :pulse] [in {world :world :as game}]
  (assoc game :world
    (simple-input-move world (:target-entity game) 3 0)))

(defmethod process-input [:P :down] [in game]
  (assoc game :saved-world (:world game)))

(defmethod process-input [:O :down] [in game]
  (-> game
    (assoc :target-entity (select-next-controllable (:world game) (:target-entity game)))
    (assoc :world (:saved-world game))))

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
(defn update-viewport [game]
  (let [world (:world game)]
    (assoc game :world
      (world/update-entity world
                           (let
                             [entity (world/get-entity world (:target-entity game))
                              viewport (first (world/get-with-comp world :viewport))]
                             (cond (> (- (:x (:position entity)) (:x (:viewport viewport)))300)
                                   (entity/update-component viewport :viewport {:x (- (:x (:position entity)) 300)})
                                   (< (- (:x (:position entity)) (:x (:viewport viewport)))100)
                                   (entity/update-component viewport :viewport {:x (- (:x (:position entity)) 100)})
                                   :default
                                   viewport
                                   )
                             )))))

(defn update-game! [game]
  (do
    (clinp/pulse!)
    (swap! game #(-> %
                       run-input
                       update-viewport
                       (map-apply-world physics/physics-system)))))

;; Why is cycle game removed from update-game?
(defn cycle-game [game]
  (do
      (update-game! game)
      (swap! game #(map-apply-world % pixi/render-system))))

;; setup animation loop
(defn animate
  "Core callback loop."
  []
  (js/requestAnimFrame animate)
  (cycle-game game-state))

(defn start []
  (do
    (swap! game-state #(map-apply-world % load-sample-world))
    (swap! game-state #(map-apply :target-entity % (partial select-next-controllable (:world %))))
    (setup-clinp!)
    (setup-pixi! game-state)
    (js/requestAnimFrame animate)
    (swap! game-state #(map-apply :world % (partial set-viewport 100 10)))
    ))

(ns sivinolk.core
  (:require [clinp.core :as clinp]
            [sivinolk.pixi :as pixi]
            [sivinolk.physics :as physics]
            [sivinolk.components :as comps]
            [sivinolk.entity :as entity]
            [sivinolk.world :as world]))

;; Create a main record to define the world
(def world-state (atom {:next-id 0 :entities {}}))

; Load some textures for entities
(def bunny-texture (js/PIXI.Texture.fromImage "images/bunny.png"))
(def ugly-block-texture (js/PIXI.Texture.fromImage "images/ugly-block.png"))

; Hack to control which entity we are moving (this really should be part of the world state?)
(def target-entity (atom 0))

(defn select-next-controllable [world current-target]
  "Select the next entity that has a controllable component. This is not smart enough to avoid infinite loops."
  (loop [t (inc current-target)]
    (let [newt (if (>= t (:next-id world)) 0 t)]
      (if (:controllable (world/get-entity world newt))
        newt
        (recur (inc newt))))))


(defn load-sample-world! []
  (do
    ;; Add a simple world block
    (swap! world-state (fn [] (world/add-entity @world-state (entity/compose-entity
                                                              [(comps/pixi-renderer. (js/PIXI.Sprite. ugly-block-texture))
                                                               (comps/position. 100 190)
                                                               (comps/aabb. 16 16)]))))

    (swap! world-state (fn [] (world/add-entity @world-state (entity/compose-entity
                                                              [(comps/pixi-renderer. (js/PIXI.Sprite. ugly-block-texture))
                                                               (comps/position. 116 190)
                                                               (comps/aabb. 16 16)]))))

    ; Add some test bunnies
    (swap! world-state (fn [] (world/add-entity @world-state (entity/compose-entity
                                                              [(comps/pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                                                               (comps/rotation. 0)
                                                               (comps/velocity. 0 3)
                                                               (comps/aabb. 26 37)
                                                               (comps/controllable.)
                                                               (comps/position. 100 100)]))))

    (swap! world-state (fn [] (world/add-entity @world-state (entity/compose-entity
                                                              [(comps/pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                                                               (comps/rotation. 0)
                                                               (comps/velocity. 0 3)
                                                               (comps/controllable.)
                                                               (comps/position. 100 100)]))))

    (swap! world-state (fn [] (world/add-entity @world-state (entity/compose-entity
                                                              [(comps/pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                                                               (comps/rotation. 0)
                                                               (comps/velocity. 10 4)
                                                               (comps/controllable.)
                                                               (comps/position. 100 100)]))))

    (swap! target-entity #(select-next-controllable @world-state 0))
    ))

;; Scratch functions for playing with user input
(defn simple-input-move! [world target-id x y]
  (let [entity (world/get-entity @world target-id)]
      (swap! world #(world/update-entity % (physics/move entity x y)))))

(defn simple-add-entity! [world]
  (swap! world #(world/add-entity @world (entity/compose-entity
                        [(comps/pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                         (comps/rotation. 0)
                         (comps/velocity. 0 3)
                         (comps/aabb. 26 37)
                         (comps/controllable.)
                         (comps/position. 100 100)]))))


; Setup for the pixi.js system
(defn setup-pixi! [world]
  (swap! world pixi/setup-world!))

(defn setup-clinp! []
  (do
    ; clinp setup and keyboard handlers
    (clinp/setup!)

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
    ))


(defn update-world! [world]
               (clinp/pulse!)
               (swap! world #(physics/physics-system %)))

;; setup animation loop
(defn animate[]
  "Core callback loop."
  (js/requestAnimFrame animate)
  (update-world! world-state)
  (pixi/render-system @world-state)
  (. (:renderer @world-state) render (:stage @world-state)))

(defn start []
  (do
    (load-sample-world!)
    (setup-clinp!)
    (setup-pixi! world-state)
    (js/requestAnimFrame animate)
    ))

(ns sivinolk.core
  (:require [clinp.core :as clinp]
            [sivinolk.pixi :as pixi]
            [sivinolk.physics :as physics]
            [sivinolk.components :as comps]
            [sivinolk.entity :as entity]
            [sivinolk.world :as world]))

; world-state works on the new entity system
(def world-state (atom {:next-id 0 :entities {}}))

(swap! world-state pixi/setup-world)


(def bunny-texture (js/PIXI.Texture.fromImage "images/bunny.png"))

(def ugly-block-texture (js/PIXI.Texture.fromImage "images/ugly-block.png"))

;; TODO Create a function to remove an entity from the world


(defn simple-add-entity! [world]
  (swap! world #(world/add-entity @world (entity/compose-entity
                        [(comps/pixi-renderer. (js/PIXI.Sprite. bunny-texture))
                         (comps/rotation. 0)
                         (comps/velocity. 0 3)
                         (comps/aabb. 26 37)
                         (comps/controllable.)
                         (comps/position. 100 100)]))))

;; Create a main record to define the world
; TODO: Stage should be provided and this whole thing should be built with a function



;; Add a simple world block
(swap! world-state (fn [] (world/add-entity @world-state (entity/compose-entity
                        [(comps/pixi-renderer. (js/PIXI.Sprite. ugly-block-texture))
                         (comps/position. 100 190)
                         (comps/aabb. 16 16)]))))

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


(defn simple-input-move! [world target-id x y]
  (let [entity (world/get-entity @world target-id)]
      (swap! world #(world/update-entity % (physics/move entity x y)))))




;(HACK-check-bound "x" (position. 110 10) {:x 100 :y 100})



(defn update-world! [world]
               (clinp/pulse!)
               (swap! world #(physics/physics-system %)))

; clinp setup and keyboard handlers
(clinp/setup!)

(defn- select-next-controllable [world current-target]
  "Select the next entity that has a controllable component. This is not smart enough to avoid infinite loops."
  (loop [t (inc current-target)]
    (let [newt (if (>= t (:next-id world)) 0 t)]
      (if (:controllable (world/get-entity world newt))
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
  (pixi/render-system @world-state)
  (. (:renderer @world-state) render (:stage @world-state)))

(js/requestAnimFrame animate)

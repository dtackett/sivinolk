;; Pixi.js system
(ns sivinolk.pixi
  (:require [vyrlynd.world :as world]))

; Screen size should be defined elsewhere
(defn setup-world!
  "This sets up the pixi.js renderer and stage."
  [world]
  (let [renderer (js/PIXI.autoDetectRenderer 400 300)
        stage (js/PIXI.Stage. 0x66ff99)]
    (do (.appendChild (.-body js/document) (.-view renderer))
         (assoc (assoc world :stage stage) :renderer renderer)
         )))

;; I feel like set-position and set-anchor could be made easier
;; if I had a grasp of macros
(defn set-position [sprite x y]
  (set! (.-position.x sprite) x)
  (set! (.-position.y sprite) y))

(defn set-anchor [sprite x y]
  (set! (.-anchor.x sprite) x)
  (set! (.-anchor.y sprite) y))

; TODO Implement some system to replace this functionality
(defn rotate
  "Change the rotate by the given delta"
  [entity delta]
  (let [sprite (:sprite entity)]
    (set! (.-rotation (:sprite entity)) (+ delta (.-rotation (:sprite entity))))))

; Pixi system functions
(defn ensure-entity-on-stage!
  "Ensure the entity is on the given stage"
  [stage entity]
  (let [sprite (js/PIXI.Sprite. (:texture (:pixi-renderer entity)))
        side (. stage addChild sprite)]
    (assoc entity :sprite sprite)))

(defn update-display
  "Update the pixi-renderer component with the current state"
  [entity viewport]
  (let [pos-comp (:position entity)
        sprite (:sprite entity)]
    (set-position
     sprite
     (- (:x pos-comp) (:x viewport))
     (- (:y pos-comp) (:y viewport)))))

(defn pixi-setup-entity [stage viewport entity]
  (if (:pixi-renderer entity)
    (update-display (ensure-entity-on-stage! stage entity) viewport)))

;; This is probably unsafe anymore, should be replaced or used in conjunction with
;; a function that will clear up the entities in the game world as well.
;; function to remove all elements from the stage
(defn empty-stage
  "Remove all Pixi DisplayObjects from the given stage."
  [stage]
  (doall (map
   (fn [target]
    ;; (.log js/console target)
     (. stage removeChild target)
     )
   ;; do a slice 0 here to copy the array as the stage array mutates with removes
   (.slice (.-children stage) 0))))

; pixi render system
(defn render-system [world]
  (let [viewport (:viewport (first (world/get-with-comp world :viewport)))]
    (do
      (empty-stage (:stage world))
      (dorun
       (map
        (partial pixi-setup-entity (:stage world) viewport)
        (vals (:entities world))))
      (. (:renderer world) render (:stage world)))
    world))



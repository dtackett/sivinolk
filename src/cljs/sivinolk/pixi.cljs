;; Pixi.js system
(ns sivinolk.pixi)

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
  (let [sprite (:sprite (:pixi-renderer entity))]
    (if (nil? (.-stage sprite))
      (. stage addChild sprite))))

(defn update-display
  "Update the pixi-renderer component with the current state"
  [entity]
  (let [pos-comp (:position entity)
        sprite (:sprite (:pixi-renderer entity))]
    (set-position sprite (:x pos-comp) (:y pos-comp))))

(defn pixi-setup-entity [stage entity]
  (if (:pixi-renderer entity)
    (do
      (ensure-entity-on-stage! stage entity)
      (update-display entity))))

; pixi render system
(defn render-system [world]
  (do
    (dorun
     (map
      (partial pixi-setup-entity (:stage world))
      (vals (:entities world))))
    (. (:renderer world) render (:stage world))))

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

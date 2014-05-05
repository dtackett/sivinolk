;; Pixi.js system
(ns sivinolk.pixi
  (:require [vyrlynd.world :as world]))

(def render-cache (atom {}))

; Screen size should be defined elsewhere
(defn setup-world!
  "This sets up the pixi.js renderer and stage."
  [world]
  (let [renderer (js/PIXI.autoDetectRenderer 400 300)
        stage (js/PIXI.Stage. 0x66ff99)]
    (do (.appendChild (.-body js/document) (.-view renderer))
      (-> world
          (assoc :stage stage)
          (assoc :renderer renderer)
          (assoc :render-cache {}))
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

; Ensure all entities in the cache still exist, if they don't remove them
; Pump entities through and build up the cache
; Give cache and entites to render system
(defn cache-entity-on-stage!
     [entity stage render-cache]
     (let [id (:id (:id entity))]
       (if-not
         (contains? render-cache id)
         (let [sprite (js/PIXI.Sprite. (:texture (:pixi-renderer entity)))]
           (do
             (. stage addChild sprite)
             (assoc render-cache id sprite)))
         render-cache)))

; Pixi system functions
(defn ensure-entity-on-stage!
  "Ensure the entity is on the given stage"
  [entity stage]
  (let [sprite (js/PIXI.Sprite. (:texture (:pixi-renderer entity)))
        side-effect (. stage addChild sprite)]
    (assoc entity :sprite sprite)))

(defn update-display
  "Update the pixi-renderer component with the current state"
  [entity render-cache viewport]
  (let [pos-comp (:position entity)
        sprite (get render-cache (:id (:id entity)))]
    (set-position
     sprite
     (- (:x pos-comp) (:x viewport))
     (- (:y pos-comp) (:y viewport)))
    render-cache))

(defn pixi-setup-entity
  [entity render-cache stage viewport]
  (if (:pixi-renderer entity)
    (update-display
       entity
       (cache-entity-on-stage! entity stage render-cache)
       viewport)
    render-cache))

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

(defn render-stage
  "This gets the pixi system to return and returns the world. Intended to make dealing with this a bit more composable."
  [world]
  (. (:renderer world) render (:stage world))
  world)

(defn remove-from-stage
  [render-cache stage entity-id]
  (do
    (if
      (not (nil? (.-parent (get render-cache entity-id))))
      (. stage removeChild (get render-cache entity-id)))
    (dissoc render-cache entity-id))
  )

(defn reconsile-cache
  "This is intended to eventually removed entities that no longer exist from the pixi stage."
  [world]
  (assoc
    world
    :render-cache
    (reduce
     (fn [render-cache entity-id]
       (cond
;        (not (contains? (:entities world) entity-id))
        :else
        (remove-from-stage render-cache (:stage world) entity-id)
        :else
        render-cache
        ))
     (:render-cache world)
     (keys (:render-cache world)))))

(defn setup-render-cache
  [world]
  (let [viewport (:viewport (first (world/get-with-comp world :viewport)))]
    (assoc
      world
      :render-cache
      (reduce
       (fn
         [render-cache entity]
         (pixi-setup-entity entity render-cache (:stage world) viewport))
       (:render-cache world)
       (vals (:entities world))))))

; pixi render system
(defn render-system [world]
  (-> world
      reconsile-cache
      setup-render-cache
      render-stage))


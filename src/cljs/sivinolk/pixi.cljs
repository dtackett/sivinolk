;; Pixi.js system
(ns sivinolk.pixi
  (:require [vyrlynd.world :as world]))

; Keep an atom of the current cache of entities setup on the stage
; This is really a shadow of what has been added to the stage
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
          (assoc :renderer renderer))
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
  "Setup the given entity entry for display"
  [entity render-cache stage viewport]
  (if (:pixi-renderer entity)
    (update-display
       entity
       (cache-entity-on-stage! entity stage render-cache)
       viewport)
    render-cache))
(defn render-stage
  "This gets the pixi system to return and returns the world. Intended to make dealing with this a bit more composable."
  [world]
  (. (:renderer world) render (:stage world))
  world)

(defn remove-from-stage
  "Remove the given entity id from the cache and the stage"
  [render-cache stage entity-id]
  (do
    (if
      (not (nil? (.-parent (get render-cache entity-id))))
      (. stage removeChild (get render-cache entity-id)))
    (dissoc render-cache entity-id))
  )

(defn reconsile-cache
  "This is intended to eventually removed entities that no longer exist from the pixi stage."
  [world render-cache]
  (reduce
   (fn [render-cache entity-id]
     (cond
      (not (contains? (:entities world) entity-id))
      (remove-from-stage render-cache (:stage world) entity-id)
      :else
      render-cache
      ))
   render-cache
   (keys render-cache)))

(defn setup-render-cache
  [world render-cache]
  (let [viewport (:viewport (first (world/get-with-comp world :viewport)))]
    (reduce
     (fn
       [render-cache entity]
       (pixi-setup-entity entity render-cache (:stage world) viewport))
     render-cache
     (vals (:entities world)))))

; pixi render system
(defn render-system [world]
  (do
    (swap! render-cache (fn [render-cache]
                          (->> render-cache
                               (reconsile-cache world)
                               (setup-render-cache world))))
    (-> world
        render-stage)))


(ns sivinolk.physics
  (:require [vyrlynd.entity :as entity]
            [vyrlynd.world :as world]))

(def world-bound {:x 500 :y 250})

(defn HACK-check-bound [keyname position world-bound]
  (let [k (keyword keyname)]
    (cond (> (k position) (k world-bound))
            (k world-bound)
          (< (k position) 0)
            0
          :default (k position))))

(defn HACK-allow-world-rejump
  [entity world-bound]
  (if (> (:y (:position entity)) (:y world-bound))
    (entity/add-component
     entity
     (merge (:controllable entity) {:jump-flag true}))
    entity))

(defn HACK-force-world-bounds [entity world-bound]
  (let [position (:position entity)]
      ; reset position to world bound
      ; TOTHINK how to handle velocity change?
      (entity/add-component
       entity
       (merge position {:x (HACK-check-bound "x" position world-bound)
                        :y (HACK-check-bound "y" position world-bound)}))))

(defn HACK-world-bounds
  [entity world-bound]
  (-> entity
      (HACK-allow-world-rejump world-bound)
      (HACK-force-world-bounds world-bound)))

(defn move
  "Move the entity by the given x and y"
  [entity x y]
  (let [position (:position entity)]
      (entity/add-component
       entity
       (merge position {:x (+ x (:x position))
                        :y (+ y (:y position))}))))

(defn apply-velocity-to-entity
  "Apply velocity to the given entity"
  [entity]
  (let [velocity (:velocity entity)]
    (move entity (:x velocity) (:y velocity))))

(defn get-bounds
  "Get the world bounds of the given entity. Returns nil if the entity does not have the appropriate components."
  [entity]
  (let [position (:position entity)
        aabb (:aabb entity)]
    (if (and position
             aabb)
      {:l (:x position)
       :t (:y position)
       :r (+ (:x position) (:w aabb))
       :b (+ (:y position) (:h aabb))})))

(defn get-midpoint
  "Get the midpoint of the given entity. Based on position and aabb."
  [entity]
  (let [position (:position entity)
        aabb (:aabb entity)]
    (if (and position aabb)
      {:x (+ (:x position) (/ (:w aabb) 2))
       :y (+ (:y position) (/ (:h aabb) 2))})))

(defn collision?
  "Check if the two given entities are in collision"
  [ea eb]
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

(defn resolve-collision
  "Crude collision resolver. Assumes that a collision has already been verified."
  [ea eb]
  (let [mida (get-midpoint ea)
        midb (get-midpoint eb)
        dx (- (:x mida) (:x midb))
        dy (- (:y mida) (:y midb))]
    (if
      (> (.abs js/Math dx) (.abs js/Math dy))
      (if (> dx 0)
        ; approach is on the horizontal
        (entity/add-component ea (merge (:position ea) {:x (+ (:x (:position eb)) (:w (:aabb eb)))}))
        (entity/add-component ea (merge (:position ea) {:x (- (:x (:position eb)) (:w (:aabb ea)))})))
      (if (> dy 0)
        ; approach is on the vertical
        (entity/add-component ea (merge (:position ea) {:y (+ (:y (:position eb)) (:h (:aabb eb)))}))
        (entity/add-component ea (merge (:position ea) {:y (- (:y (:position eb)) (:h (:aabb ea)))})))
      )))

(defn hacky-extra-resolve
  "Hacky extra resolution."
  [ea eb]
  (if (:controllable ea)
    (entity/add-component ea (merge (:controllable ea) {:jump-flag true}))))

(defn resolve-collisions [entity world]
  (reduce
   (fn [entity test-entity]
     (if (collision? entity test-entity)
       (-> entity
         (resolve-collision test-entity)
         (hacky-extra-resolve test-entity))
       entity))
   entity
   (vals (:entities world))
   ))

(comment
  (match [collider collidee direction]
     [:controllable :world :above] (reset-state (:controllable collider))
  )

  "This could get out of hand very quickly. How can we quickly and easily setup the desired behaviors without making it unclear as to what is occuring and without making a giant function handle all the bits?"
)

(defn do-physics-simulation
  "Apply physics simulation to the given entity"
  [entity world]
  ; apply velocity
  (if (:velocity entity)
    (-> entity
        apply-velocity-to-entity
        (resolve-collisions world)
        (HACK-world-bounds world-bound)))
  ; TODO check for collisions
  ; TODO resolve collisions
  ; TOTHINK what to do with collision events?
  )

; physics system
(defn physics-system [world]
  (reduce
   (fn [world entity] (world/update-entity world (do-physics-simulation entity world)))
    world
    (vals (:entities world))))

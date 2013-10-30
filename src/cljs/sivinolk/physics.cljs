(ns sivinolk.physics
  (:require [sivinolk.entity :as entity]
            [sivinolk.world :as world]))

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
      (entity/add-component
       entity
       (merge position {:x (HACK-check-bound "x" position world-bound)
                        :y (HACK-check-bound "y" position world-bound)}))))

(defn move [entity x y]
  "Move the entity by the given x and y"
  (let [position (:position entity)]
      (entity/add-component
       entity
       (merge position {:x (+ x (:x position))
                        :y (+ y (:y position))}))))

(defn apply-velocity-to-entity [entity]
  "Apply velocity to the given entity"
  (let [velocity (:velocity entity)]
    (move entity (:x velocity) (:y velocity))))

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
        (entity/add-component ea (merge (:position ea) {:x (+ (:x (:position eb)) (:w (:aabb eb)))}))
        (entity/add-component ea (merge (:position ea) {:x (- (:x (:position eb)) (:w (:aabb ea)))})))
      (if (> dy 0)
        ; approach is on the vertical
        (entity/add-component ea (merge (:position ea) {:y (+ (:y (:position eb)) (:h (:aabb eb)))}))
        (entity/add-component ea (merge (:position ea) {:y (- (:y (:position eb)) (:h (:aabb ea)))})))
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
   (fn [world entity] (world/update-entity world (do-physics-simulation entity world)))
    world
    (vals (:entities world))))

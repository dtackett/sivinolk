(ns sivinolk.spec.physics
    (:require [specljs.core :as speclj]
              [vyrlynd.entity :as entity]
              [vyrlynd.components :as core-comps]
              [sivinolk.components :as comps]
              [sivinolk.physics :as physics])
    (:require-macros [specljs.core :refer [describe it should should-not should-be-nil should=]]))

;; TODO These tests work on idividual entities
;; there are no tests which operate on the system as a whole (and there should be)

(defn create-entity
  ([x y w h] (create-entity x y w h 1))
  ([x y w h id]
   (entity/compose-entity [(comps/position. x y)
                           (comps/aabb. w h)
                           (core-comps/id. id)])))

(describe "Getting current bounds"
          (it "Get bounds for an entity with a position and an aabb"
              (should= {:l 10 :t 10 :r 20 :b 20}
                       (physics/get-bounds (create-entity 10 10 10 10))))

          (it "Get bounds for an entity with just a position"
              (should-be-nil
               (physics/get-bounds
                (entity/compose-entity [(comps/position. 10 10)]))))

          (it "Get bounds for an entity with just an aabb"
              (should-be-nil
               (physics/get-bounds
                (entity/compose-entity [(comps/aabb. 10 10)])))))

(describe "Getting the midpoint"
          (it "Get midpoint for an entity with a position "
              (should= {:x 7.5 :y 7.5}
                        (physics/get-midpoint (create-entity 5 5 5 5))))

          (it "Get midpoint for an entity with just a position"
              (should-be-nil
               (physics/get-midpoint
                (entity/compose-entity [(comps/position. 10 10)]))))

          (it "Get midpoint for an entity with just an aabb"
              (should-be-nil
               (physics/get-midpoint
                (entity/compose-entity [(comps/aabb. 10 10)])))))

(describe "Collision tests"
          (it "Two non-colliding entities"
              (should-not
               (physics/collision?
                (create-entity 0 0 10 10 0)
                (create-entity 50 50 10 10 1))))

          (it "Collider colliding from above"
              (should
               (physics/collision?
                (create-entity 0 8 10 10 0)
                (create-entity 0 15 10 10 1))))

          (it "Collider colliding from left"
              (should
               (physics/collision?
                (create-entity 8 0 10 10 0)
                (create-entity 15 0 10 10 1))))

          (it "Collider colliding from right"
              (should
               (physics/collision?
                (create-entity 23 0 10 10 0)
                (create-entity 15 0 10 10 1))))

          (it "Collider colliding from bottom"
              (should
               (physics/collision?
                (create-entity 0 23 10 10 0)
                (create-entity 0 15 10 10 1)))))

(describe "Collision resolution"
          (it "Collision from the top"
              (should=
               {:x 0 :y 5}
               (:position
                (physics/resolve-collision
                 (create-entity 0 8 10 10)
                 (create-entity 0 15 10 10)))))

          (it "Collision from the bottom"
              (should=
               {:x 0 :y 25}
               (:position
                (physics/resolve-collision
                 (create-entity 0 23 10 10)
                 (create-entity 0 15 10 10)))))

          (it "Collision from the left"
              (should=
               {:x 5 :y 0}
               (:position
                (physics/resolve-collision
                 (create-entity 8 0 10 10)
                 (create-entity 15 0 10 10)))))

          (it "Collision from the right"
              (should=
               {:x 25 :y 0}
               (:position
                (physics/resolve-collision
                 (create-entity 23 0 10 10)
                 (create-entity 15 0 10 10))))))

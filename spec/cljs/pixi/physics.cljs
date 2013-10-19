(ns pixi.physics.test
    (:require [specljs.core :as speclj]
              [pixi-cljs.core :as pixi])
    (:require-macros [specljs.core :refer [describe it should should-not should-be-nil should==]]))

;; TODO These tests work on idividual entities
;; there are no tests which operate on the system as a whole (and there should be)

(defn create-entity
  ([x y w h] (create-entity x y w h 1))
  ([x y w h id]
   (pixi/compose-entity [(pixi/position. x y)
                         (pixi/aabb. w h)
                         (pixi/id. id)])))

(describe "Getting current bounds"
          (it "Get bounds for an entity with a position and an aabb"
              (should== {:l 10 :t 10 :r 20 :b 20}
                       (pixi/get-bounds (create-entity 10 10 10 10))))

          (it "Get bounds for an entity with just a position"
              (should-be-nil
               (pixi/get-bounds
                (pixi/compose-entity [(pixi/position. 10 10)]))))

          (it "Get bounds for an entity with just an aabb"
              (should-be-nil
               (pixi/get-bounds
                (pixi/compose-entity [(pixi/aabb. 10 10)])))))

(describe "Getting the midpoint"
          (it "Get midpoint for an entity with a position "
              (should== {:x 7.5 :y 7.5}
                        (pixi/get-midpoint (create-entity 5 5 5 5))))

          (it "Get midpoint for an entity with just a position"
              (should-be-nil
               (pixi/get-midpoint
                (pixi/compose-entity [(pixi/position. 10 10)]))))

          (it "Get midpoint for an entity with just an aabb"
              (should-be-nil
               (pixi/get-midpoint
                (pixi/compose-entity [(pixi/aabb. 10 10)])))))

(describe "Collision tests"
          (it "Two non-colliding entities"
              (should-not
               (pixi/collision?
                (create-entity 0 0 10 10 0)
                (create-entity 50 50 10 10 1))))

          (it "Collider colliding from above"
              (should
               (pixi/collision?
                (create-entity 0 8 10 10 0)
                (create-entity 0 15 10 10 1))))

          (it "Collider colliding from left"
              (should
               (pixi/collision?
                (create-entity 8 0 10 10 0)
                (create-entity 15 0 10 10 1))))

          (it "Collider colliding from right"
              (should
               (pixi/collision?
                (create-entity 23 0 10 10 0)
                (create-entity 15 0 10 10 1))))

          (it "Collider colliding from bottom"
              (should
               (pixi/collision?
                (create-entity 0 23 10 10 0)
                (create-entity 0 15 10 10 1)))))

(describe "Collision resolution"
          (it "Collision from the top"
              (should==
               {:x 0 :y 5}
               (:position
                (pixi/resolve-collision
                 (create-entity 0 8 10 10)
                 (create-entity 0 15 10 10)))))

          (it "Collision from the bottom"
              (should==
               {:x 0 :y 25}
               (:position
                (pixi/resolve-collision
                 (create-entity 0 23 10 10)
                 (create-entity 0 15 10 10)))))

          (it "Collision from the left"
              (should==
               {:x 5 :y 0}
               (:position
                (pixi/resolve-collision
                 (create-entity 8 0 10 10)
                 (create-entity 15 0 10 10)))))

          (it "Collision from the right"
              (should==
               {:x 25 :y 0}
               (:position
                (pixi/resolve-collision
                 (create-entity 23 0 10 10)
                 (create-entity 15 0 10 10))))))

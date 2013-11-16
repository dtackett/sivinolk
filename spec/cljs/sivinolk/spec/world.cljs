(ns sivinolk.spec.world
    (:require [specljs.core :as speclj]
              [sivinolk.entity :as entity]
              [sivinolk.components :as comps]
              [sivinolk.world :as world])
    (:require-macros [specljs.core :refer [describe it should should-not should-be-nil should==]]))

;; Find entities by component (no entities with component)
;; Find entities by component (single entity with component)
;; Find entities by component (multiple entities with component)
;; Add entity (without an id)
;; Add entity (with an id)
;; Update entity (with an id)
;; Update entity (with an id but doesn't exit)
;; Update entity (without an id)

(describe "Getting a specific entity"
          (it "by an existant id"
              (let [resp (world/add-entity
                          world/base-world
                          (entity/compose-entity [(comps/position. 5 5)]))
                    entity (:entity resp)
                    test-world (:world resp)]
                (should== entity (world/get-entity test-world 0))))

          (it "by a non-existant id"
              (should-be-nil
               (world/get-entity world/base-world 0))))

(describe "Getting entities based on component"
          (it "when no entities exist with the given component"
              (let [resp (world/add-entity
                          world/base-world
                          (entity/compose-entity [(comps/position. 5 5)]))
                    entity (:entity resp)
                    test-world (:world resp)]
                (should==
                 '()
                 (world/get-with-comp test-world :aabb))))

          (it "when no entities exist"
              (should==
               '()
               (world/get-with-comp world/base-world :aabb)))

          (it "when the world only contains entities with the component"
              (let [resp (world/add-entity
                          world/base-world
                          (entity/compose-entity [(comps/position. 5 5)]))
                    entity (:entity resp)
                    test-world (:world resp)]
                (should==
                 (list entity)
                 (world/get-with-comp test-world :position))))

          (it "when some entities contain the component"
              (let [resp (world/add-entity
                          world/base-world
                          (entity/compose-entity [(comps/position. 5 5)]))
                    entity (:entity resp)
                    test-world (:world (world/add-entity (:world resp) (entity/compose-entity [(comps/aabb. 5 5)])))]
                (should==
                 (list entity)
                 (world/get-with-comp test-world :position)))))

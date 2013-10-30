(ns sivinolk.world
  (:require [sivinolk.entity :as entity]
            [sivinolk.components :as comps]))

;; World should have a list of all entities in the world
;; World should be attached to a stage?
;; Each entity should have some unique id
;; Create a function to find an entity based on id in a world
(defn get-entity [world entity-id]
  (get (:entities world) entity-id))

;; Create a function to add an entity to the world
(defn add-entity [world entity]
  (let [entity-id (:next-id world)
        ; Add an id component to the entity
        entity (entity/add-component entity (comps/id. entity-id))
        entities (assoc (:entities world) entity-id entity)]
      (assoc world
        :next-id (inc entity-id)
        :entities entities)))

; TODO This should blow up gracefully if the entity does not have an id component
(defn update-entity [world entity]
  "Generate a new world state from the given updated entity state."
  (assoc
    world
    :entities
    (assoc
      (:entities world)
      (-> entity :id :id)
      entity)))

;; TODO Create a function to remove an entity from the world

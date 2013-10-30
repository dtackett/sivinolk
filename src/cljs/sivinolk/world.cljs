(ns sivinolk.world
  (:require [sivinolk.entity :as entity]
            [sivinolk.components :as comps]))

;; World should be attached to a stage?
(defn get-entity [world entity-id]
  "Get the entity in the world with the given entity id"
  (get (:entities world) entity-id))

;; Create a function to add an entity to the world
;; TODO What to do if adding an entity that already has an id?
(defn add-entity [world entity]
  "Generate a new world state where the given entity is added to the world
  data. This will generate and assign an id component to the entity."
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


;; Systems seem to need to have some state
;; The System state seems like it would be good to store in the world state
;; Systems could use hooks for startup and shutdown
;; Systems would also probably like hooks for entities being added/removed
;; Systems could also benefit from a 'tick' on the main world update
;; or maybe the better thing is to be able to get a list of the systems from the world

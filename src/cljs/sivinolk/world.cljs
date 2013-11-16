(ns sivinolk.world
  (:require [sivinolk.entity :as entity]
            [sivinolk.components :as comps]))

;; TOTHINK: Should systems have a hook when entities are added/removed?

(def base-world {:next-id 0 :entities {}})

;; World should be attached to a stage?
(defn get-entity
  "Get the entity in the world with the given entity id"
  [world entity-id]
  (get (:entities world) entity-id))

;; Create a function to add an entity to the world
;; TODO What to do if adding an entity that already has an id?
;; TODO There should be a better way than doing this tuple thing?
(defn add-entity
  "Generate a new world state where the given entity is added to the world
  data. This will generate and assign an id component to the entity. This returns
  a tuple of the new world state and the entity state that was just modified."
  [world entity]
  (let [entity-id (:next-id world)
        ; Add an id component to the entity
        entity (entity/add-component entity (comps/id. entity-id))
        entities (assoc (:entities world) entity-id entity)]
    {:world (assoc world
              :next-id (inc entity-id)
              :entities entities)
     :entity entity}))

; TODO This should blow up gracefully if the entity does not have an id component
(defn update-entity
  "Generate a new world state from the given updated entity state."
  [world entity]
  (assoc
    world
    :entities
    (assoc
      (:entities world)
      (-> entity :id :id)
      entity)))

(defn get-with-comp
  "Get a collection of all the entities with the given component"
  [world component]
  (filter #(entity/has-component % component) (vals (:entities world))))

;; TODO Create a function to remove an entity from the world

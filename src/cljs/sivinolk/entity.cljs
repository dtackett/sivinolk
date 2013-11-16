(ns sivinolk.entity
  (:require [sivinolk.components :as components]))

; Entities
; [the entities in our system]

; Entity composition functions
(defn add-component
  "Add a component by its name to the given map"
  [entity component]
  (assoc entity (components/component-name component) component))

(defn compose-entity [components]
  (reduce add-component {} components))

(defn has-component [entity component]
  (contains? entity component))

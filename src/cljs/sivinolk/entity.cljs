(ns sivinolk.entity
  (:require [sivinolk.components :as components]))

; Entities
; [the entities in our system]

; Entity composition functions
(defn add-component [e c]
  "Add a component by its name to the given map"
  (assoc e (components/component-name c) c))

(defn compose-entity [components]
  (reduce add-component {} components))

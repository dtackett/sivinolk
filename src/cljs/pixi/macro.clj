(ns pixi.macro)

(defmacro component [comp-name props & r]
  `(defrecord
     ~(symbol (name comp-name)) ~props
     pixi.components/component-proto
       (~(symbol "component-name") [~(symbol "_")] ~(keyword comp-name))
     ~@r))

(macroexpand-1 '(component :position [x y z]))

; crude implementation could loop through all entities and see if the needed
; components are available. If they are then the system should operate on it.
; A optimization could be to test when entities are added to the world. Each
; system could have its own list (which is really just an id of the entity)
; (instance? position my-position)

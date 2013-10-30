(ns sivinolk.macro)

(defmacro defcomponent [comp-name props & r]
  `(defrecord
     ~(symbol (name comp-name)) ~props
     sivinolk.components/component-proto
       (~(symbol "component-name") [~(symbol "_")] ~(keyword comp-name))
     ~@r))

; (macroexpand-1 '(component :position [x y z]))

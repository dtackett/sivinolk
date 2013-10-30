(ns sivinolk.components
  (:require-macros [sivinolk.macro :refer [component]]))

(defprotocol component-proto (component-name [_]))

; Components
; (Some components are dependent on the existance of others)
; position component [x, y]
; collision component [aabb]
; physics component [vx, vy, ax, ay]
; render component [sprite]

(component pixi-renderer [sprite])
(component position [x y])
(component rotation [r]) ; Currently nothing pays attention to the rotation
(component velocity [x y])
(component aabb [w h])      ; axis aligned bounding box
(component controllable []) ; Whether the entity can be controlled
(component id [id])

(ns sivinolk.components
  (:require-macros [sivinolk.macro :refer [defcomponent]]))

(defprotocol component-proto (component-name [_]))

; Components
; (Some components are dependent on the existance of others)
; position component [x, y]
; collision component [aabb]
; physics component [vx, vy, ax, ay]
; render component [sprite]

(defcomponent pixi-renderer [sprite])
(defcomponent position [x y])
(defcomponent rotation [r]) ; Currently nothing pays attention to the rotation
(defcomponent velocity [x y])
(defcomponent aabb [w h])      ; axis aligned bounding box
(defcomponent controllable [start-jump-time]) ; Whether the entity can be controlled
(defcomponent id [id])

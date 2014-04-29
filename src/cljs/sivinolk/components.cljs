(ns sivinolk.components
  (:require-macros [vyrlynd.macro :refer [defcomponent]]))

; Components
; (Some components are dependent on the existance of others)
; position component [x, y]
; collision component [aabb]
; physics component [vx, vy, ax, ay]
; render component [sprite]

(defcomponent pixi-renderer [texture])
(defcomponent position [x y])
(defcomponent rotation [r]) ; Currently nothing pays attention to the rotation
(defcomponent velocity [x y])
(defcomponent aabb [w h])      ; axis aligned bounding box
(defcomponent controllable [start-jump-time jump-flag]) ; Whether the entity can be controlled

(defcomponent viewport [x y]) ; viewport offset
(defcomponent world-bounds [w h]) ; world component

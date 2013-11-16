(ns sivinolk.system)


;; Systems seem to need to have some state
;; The System state seems like it would be good to store in the world state
;; Systems could use hooks for startup and shutdown
;; Systems would also probably like hooks for entities being added/removed
;; Systems could also benefit from a 'cycle' on the main world update
;; or maybe the better thing is to be able to get a list of the systems from the world

#_(def test-system {:cycle (fn [world-state] nil)
                  :setup (fn [world-state] nil)
                  :teardown (fn [world-state] nil)})

#_(add-system world-state pixi)

#_(setup-systems world-state)

#_(teardown-systems world-state)

#_(cycle-systems world-state)


;; Thoughts on a camera system
;; Needs to understand the target entity
;; Needs to understand the viewport
;; As the target entity moves the viewport should move
;; Rendering should take into account the viewport

(defprotocol System
  (step [world] "Perform a simulation step on the given world")
  (setup [world] "Initial setup call for the system")
  (teardown [world] "Teardown call to clean up after a system"))

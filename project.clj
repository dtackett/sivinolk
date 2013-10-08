(defproject pixi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  ;; CLJ source code path
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"]
                 [compojure "1.1.5"]]

  ;; lein-cljsbuild plugin to build a CLJS project
  :plugins [
  			;; cljsbuild plugin
  			[lein-cljsbuild "0.3.3"]

  			;; ring plugin
  			[lein-ring "0.8.7"]]

  :ring {:handler pixi.handler/app}

  ;; cljsbuild options configuration
  :cljsbuild {:builds
              [{;; CLJS source code path
                :source-paths ["src/cljs"]

                ;; Google Closure (CLS) options configuration
                :compiler {;; CLS generated JS script filename
                           :output-to "resources/public/js/cljs-pixi.js"

                           ; See http://lukevanderhart.com/2011/09/30/using-javascript-and-clojurescript.html for why advanced optimization doesn't work.

                           ;; minimal JS optimization directive
                           :optimizations :whitespace

                           ;; generated JS code prettyfication
                           :pretty-print true}}]}

  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})

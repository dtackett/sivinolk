# sivinolk cljs

This is an experiment in using [Pixi.js](http://www.pixijs.com/) and [ClojureScript](http://clojurescript.com/) to create a Component Entity System based game.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

There are two build profiles. One for development which contains tests and one for production which omits the tests.

To compile the clojurescript run:

	lein cljsbuild once [profile]

To setup auto complication run:

	lein cljsbuild auto [profile]

If the dev profile is uses then the tests will be re-run after each build.


To start a web server for the application, run:

    lein ring server-headless

## License

Copyright © 2013 Devon Tackett

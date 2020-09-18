(ns frutil.spa.devtools.initialize
  (:require
   [frutil.spa.devtools.core :as devtools]
   [frutil.spa.devtools.console :as console]))

(js/console.log "Initializing Frutil DevTools")
(devtools/initialize! [console/Console])

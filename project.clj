(require '[clojure.java.io :as io])

(defn tools-jar []
  ;; for some reason the JRE often ends up on JAVA_HOME. bit of a hack
  (.replace (.getCanonicalPath (io/file (System/getProperty "java.home")
                                "lib" "tools.jar"))
           "jre"
           "jdk"))

(defproject duck "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [reagent "0.5.0"]
                 [compojure "1.3.4"]
                 [hiccup "1.0.5"]
                 [ring "1.3.2"]
                 [ring-server "0.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [cljs-ajax "0.5.1"]
                 [secretary "1.2.3"]
                 [net.lingala.zip4j/zip4j "1.3.2"]
                 [quil "2.2.6"]
                 [cheshire "5.5.0"]
                 [me.raynes/fs "1.4.6"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]


  ;; CUSTOM DOCLET COMPILE
  :aot [duck.doclet]
  :resource-paths [#=(tools-jar) "resources"]
  :uberjar {:aot :all}
  ;;:uberjar-name "duck-doclet.jar"


  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload "duck.core/on-js-reload"}
                        :compiler {:main duck.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/duck.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/duck.js"
                                   :main duck.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS
             ;; :nrepl-port 7888 ;; Start an nREPL server into the running figwheel process

             ;; Server Ring Handler (optional) if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this doesn't work for you just run your own server :
             :ring-handler duck.server/handler})

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"

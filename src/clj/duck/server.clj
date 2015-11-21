(ns duck.server
  (:require
        [duck.github :as github]
        [compojure.core :refer [defroutes GET POST]]
        [compojure.route :refer [not-found resources]]
        [compojure.handler :refer [site]] ; ??? form, query params decode; cookie; session, etc
        [hiccup.core :refer [html]]
        [ring.middleware.reload :refer [wrap-reload]]
        [ring.middleware.defaults :refer [site-defaults wrap-defaults api-defaults]]
        [ring.middleware.json :refer [wrap-json-response]]
        [ring.util.response :refer [response]])
   (:import com.sun.tools.javadoc.Main))

; runs the javadoc tool, no shell required!
; path should be the directory in which the first part of a namespace like "org"/"com"/"net" is located.
; subpackages can be "org"/"com"/"net", or whatever happens to be the first part of the package
(defn run-javadoc [path subpackages]
  (Main/execute (into-array ["-doclet" "DuckDoclet" "-sourcepath" path "-subpackages" subpackages])))

; find the entry directory so we can run javadoc from there
; just a hardcoded path right now
(defn javadoc-entry-path [root]
  (str root "my-app\\src\\main\\java"))

(defn route-javadoc [url]
  (if-let [path (github/download-github-repo url)]
    (response {:foo (str "echo: " url "  |  unzipped folder name: " path "  |  javadoc output: " (do
                                                                                                   (-> url javadoc-entry-path (run-javadoc "com"))
                                                                                                   (slurp "flubber.txt")))})
    (response {:foo (str "echo: " url "  |  something didn't work out. Invalid url? Github offline? ")})))

(defroutes routes
  (POST "/javadoc" [url] (do
                           (println "git url: " url)
                           (route-javadoc url))))

(def handler
  (-> #'routes
      (wrap-defaults (assoc site-defaults :security :anti-forgery)) ; remove anti-forgery stuff
      wrap-json-response
      wrap-reload))

;(def link (github/download-github-repo "https://github.com/pdurbin/maven-hello-world"))
;(run-javadoc (javadoc-entry-path link) "com")

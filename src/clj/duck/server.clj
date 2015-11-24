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
        [ring.util.response :refer [response]]
        [clojure.java.io :as io]
        [me.raynes.fs :as fs])
  (:import com.sun.tools.javadoc.Main))

; Runs the javadoc tool, no shell required!
; path should be the directory in which the first part of a namespace like "org"/"com"/"net" is located.
; subpackages can be "org"/"com"/"net", or whatever happens to be the first part of the package
(defn run-javadoc [path subpackages]
  (let [run-info (str "\n\n"
                      "\n*************************"
                      "\nRunning javadoc with"
                      "\npath: " path
                      "\nsubpackages: " subpackages
                      "\n\n*************************"
                      "\n\n")]
    (println run-info)
    (spit "last-run.txt" run-info)
    (Main/execute (into-array ["-docletpath"  "target/classes"
                               "-doclet"      "DuckDoclet"
                               "-sourcepath"  path
                               "-subpackages" subpackages]))))

; Looks for the first occurence of a directory named dir-name.
; RETURNS NIL if no src dir found.
(defn find-directory [dir-name path]
  (if-let [src-dir (first (->> (fs/iterate-dir path) ; each iteration returns a [Root, Dirs, Files] list
                               (filter #(= dir-name (.getName (first %)))) ; compare dir-name with the Root element of the list
                               (map #(first %))))] ; extract the Root out of [Root, Dirs, Files]
    (.getCanonicalPath src-dir)))

; src-path: The path to the src directory of a single project.
; returns the maven path only if the src-folder has the subdir: \\main\\java
(defn wrap-maven-project [src-path]
  (let [maven-path (str src-path "\\main\\java")]
    (if (fs/exists? maven-path)
      maven-path
      src-path)))

; Find the entry directory so we can run javadoc from there.
; returns nil when there is no src folder
;
; normal return value example:
;  (str root "my-app\\src")))
;
; maven-project return value example:
;  (str root "my-app\\src\\main\\java")))
;
(defn javadoc-entry-path [root]
  (some->> root
          (find-directory "src") ; can return nil
          (wrap-maven-project)))

; Returns the names of all the direct child directories of entry-path
(defn get-subpackages [entry-path]
  (clojure.string/join " " (->> entry-path
                                (fs/list-dir) ; Also lists files, somehow..
                                (filter #(.isDirectory %))
                                (filter #(not (= "META-INF" (.getName %)))) ; Can also filter this after the map
                                (map #(.getName %)))))

(defn route-javadoc [url]
  (println "git url: " url)
  (if-let [unzip-path (github/download-github-repo url)]
    (if-let [entry-path (javadoc-entry-path unzip-path)]
      (let [javadoc-exit-code (run-javadoc entry-path (get-subpackages entry-path))
            javadoc-output (slurp "flubber.txt")]
        (response {:foo (str "echo: " url "  |  entry path: " entry-path "  |  javadoc output: " javadoc-output)}))
      (response {:foo (str "echo: " url "  |  something didn't work out. Couldn't find a src directory.")}))
    (response {:foo (str "echo: " url "  |  something didn't work out. Invalid url? Github offline? ")})))

(defn test-route []
  (let [entry-path (javadoc-entry-path "\\downloads")
        subpackages (get-subpackages entry-path)]
    (println "entry-path  = " entry-path)
    (println "subpackages = " subpackages)
    (response {:foo "lll"})))

(defroutes routes
  (POST "/test" [] (test-route))
  (POST "/javadoc" [url] (route-javadoc url)))

(def handler
  (-> #'routes
      (wrap-defaults (assoc site-defaults :security :anti-forgery)) ; remove anti-forgery stuff
      wrap-json-response
      wrap-reload))



      

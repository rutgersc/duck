(ns duck.github)

;takes a file on disk and a path, extracts entire zip
(defn- unzip-dir!
  [file dest-path]
  (try
    (let [zip-file (net.lingala.zip4j.core.ZipFile. file)]
      (.extractAll zip-file dest-path)

      (str
       dest-path
       (.. zip-file getFileHeaders (get 0) getFileName)))
    (catch Exception e (.getMessage e))))

;transforms base repo url to downloadable archive url
(defn- github->api-link [link]
  (str link "/zipball/master"))

;take url and downloads to file
(defn- download! [uri file]
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)
    file))



;takes a github base url and returns the path of the new folder
; (download-github-repo "https://github.com/pallix/tikkba")
;  => "C:\\projects\\duck\\downloads\\pallix-tikkba-3d2d930/"
(defn download-github-repo [base-url]
  (let [rand-name (str (rand-int 5000))
        dir (str (System/getProperty "user.dir") "\\downloads\\")]
    (-> base-url
        github->api-link
        (download! (str dir rand-name ".zip"))
        (unzip-dir! dir))))

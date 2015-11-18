(ns duck.server
  (:require
        [duck.github]
        [compojure.core :refer [defroutes GET POST]]
        [compojure.route :refer [not-found resources]]
        [compojure.handler :refer [site]] ; ??? form, query params decode; cookie; session, etc
        [hiccup.core :refer [html]]
        [ring.middleware.reload :refer [wrap-reload]]
        [ring.middleware.defaults :refer [site-defaults wrap-defaults api-defaults]]
        [ring.middleware.json :refer [wrap-json-response]]
        [ring.util.response :refer [response]]))


(defn route-javadoc [url]
  (response {:foo (str "echo: " url)}))

(defroutes routes
  (POST "/javadoc" [url] (do
                           (println "git url: " url)
                           (route-javadoc url))))

(def handler
  (-> #'routes
      (wrap-defaults (assoc site-defaults :security :anti-forgery)) ; remove anti-forgery stuff
      wrap-json-response
      wrap-reload))

(ns duck.server
  (:require
        [duck.github]
        [compojure.core :refer [defroutes GET POST]]
        [compojure.route :refer [not-found resources]]
        [compojure.handler :refer [site]] ; ??? form, query params decode; cookie; session, etc
        [hiccup.core :refer [html]]
        [ring.middleware.reload :as reload]
        [ring.middleware.defaults :refer [site-defaults wrap-defaults api-defaults]]
        [ring.middleware.json :refer [wrap-json-response]]
        [ring.util.response :refer [response]]))


;this should be in the cljs part
(defn test-page [req]
  (html
   [:html [:body [:form {:method "post" :action "/javadoc"}
      ;(anti-forgery-field)
                  "What's your name?" [:input {:type "text" :name "url"}] [:input {:type "submit"}]]]]))

(defn route-javadoc [url]
  (response {:foo (str "echo: " url)}))

(defroutes routes
  (GET "/" [] (slurp "resources/public/index.html"))
  (GET "/test" [] test-page)
  (POST "/javadoc" [url] (do
                           (println "git url: " url)
                           (route-javadoc url)))
  (resources "/"))

(def handler
    (reload/wrap-reload (wrap-defaults #'routes api-defaults)))


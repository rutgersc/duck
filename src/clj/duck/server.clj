(ns duck.server
  (:require
        [duck.github]
        [compojure.core :refer [defroutes GET POST]]
        [compojure.route :refer [not-found resources]]
        [compojure.handler :refer [site]] ; ??? form, query params decode; cookie; session, etc
        [hiccup.core :refer [html]]
        [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
        [ring.middleware.json :refer [wrap-json-response]]
        [ring.util.response :refer [response]]))


(defn test-page [req]
  (html
   [:html [:body [:form {:method "post" :action "/javadoc"}
      ;(anti-forgery-field)
                  "What's your name?" [:input {:type "text" :name "url"}] [:input {:type "submit"}]]]]))

(defn route-javadoc [url]
  (response {:foo (str "echo: " url)}))

(defroutes routes
  (GET "/test" [] test-page)
  (POST "/javadoc" [url] (do
                           (println "git url: " url)
                           (route-javadoc url))))

(def handler
  (-> routes
      (wrap-defaults (assoc site-defaults :security false)) ; remove anti-forgery stuff
      wrap-json-response))

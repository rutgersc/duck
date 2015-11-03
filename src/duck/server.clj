(ns duck.server
	(:require
	      [compojure.core :refer [defroutes GET POST]]
	      [compojure.route :refer [not-found resources]]
	 		  [compojure.handler :refer [site]] ; ??? form, query params decode; cookie; session, etc
	 			[hiccup.core :refer [html]]
	 		  [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn test-page [req]
	(html
	 [:html [:body [:form {:method "post" :action "/javadoc"}
			;(anti-forgery-field)
			"What's your name?" [:input {:type "text" :name "url"}] [:input {:type "submit"}]]]]))

(defn show-landing-page [req] ;; ordinary clojure function, accepts a request map, returns a response map
	;; return landing page's html string. Using template library is a good idea:
	;; mustache (https://github.com/shenfeng/mustache.clj, https://github.com/fhd/clostache...)
	;; enlive (https://github.com/cgrand/enlive)
	;; hiccup(https://github.com/weavejester/hiccup)
	(test-page))

(defn route-javadoc [name]          ;; ordinary clojure function
	(str "Thanks " name))

(defroutes routes
	(GET "/" [] show-landing-page)
	(GET "/test" [] test-page)
	(POST "/javadoc" [url] (route-javadoc url))
	(resources "/")
	(not-found "<p>Page not found.</p>")) ;; all other, return 404

(def handler
	(wrap-defaults routes (assoc site-defaults :security false)))

;(run-server (site #'all-routes) {:port 8080})

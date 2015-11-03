(ns ^:figwheel-always duck.core
  (:require [reagent.core :as reagent :refer [atom]]
						[goog.net.XhrIo :as xhr]
						[ajax.core :refer [GET POST]]
))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hellod world!"}))

(defn hello-world []  [:h1 (:text @app-state)])
(reagent/render-component [hello-world] (. js/document (getElementById "app")))


(defn get-json-javadoc [git-url success]
	(POST "/javadoc"
        {:body (str "url=" git-url)
				 :response-format :json
				 :keywords? true
         :handler success
         :error-handler (fn [err]
										(.log js/console err))}))

(defn on-js-reload []

	(get-json-javadoc
	 	"https://github.com/weavejester/ring-json-response"
  	(fn [data]
			(println (:foo data))))

	(swap! app-state assoc :text "yolo")
)


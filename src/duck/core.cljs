(ns ^:figwheel-always duck.core
  (:require [reagent.core :as reagent :refer [atom]]
						[goog.net.XhrIo :as xhr]
))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hellod world!"}))

(defn hello-world []  [:h1 (:text @app-state)])
(reagent/render-component [hello-world] (. js/document (getElementById "app")))



(defn get-json-javadoc [project-git-url success]
	(xhr/send "/javadoc" success "POST" (str "url=" project-git-url)))
;goog.net.XhrIo.send(url, opt_callback, opt_method, opt_content, opt_headers, opt_timeoutInterval)
;https://closure-library.googlecode.com/git-history/docs/class_goog_net_XhrIo.html


(defn on-js-reload []

	(get-json-javadoc
	 	"https://github.com/weavejester/ring-json-response"
	 	(fn [reply]
			;(.log js/console reply)
			(.log js/console (.getResponse (.-target reply)))))

	(swap! app-state assoc :text "yolo")
)


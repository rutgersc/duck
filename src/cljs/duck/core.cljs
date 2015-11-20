(ns ^:figwheel-always duck.core
  (:require [reagent.core :as reagent]
            [goog.net.XhrIo :as xhr]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [duck.quil :as duck-quil]
            [quil.core :as quil-core :include-macros true]
            [quil.middleware :as m]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (reagent/atom
                     {:header-title "Duck Project!"
                      :javadoc-response ""
                      :github-url "https://github.com/pallix/tikkba"
                      :paused false
                      :width 500
                      :height 500}))

(defn get-json-javadoc [git-url success]
  (POST "/javadoc"
        {:body (str "url=" git-url)
         :response-format :json
         :keywords? true
         :handler success
         :error-handler (fn [err]
                         (.log js/console err))}))

(defn github-url? [url]
  (re-matches #"https?://github.com/.*/.*/?" url))

(defn github-url->css-class [url]
  (if (nil? (github-url? url))
    "incorrect-url"
    "correct-url"))

(defn set-quil-pause! [pause]
  (swap! app-state assoc :paused pause)
  (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
    (if pause
      (quil-core/no-loop)
      (quil-core/start-loop))))

(quil-core/defsketch javadoc-sketch
              :host "javadoc-canvas"
              :size [(.-innerWidth  js/window) (.-innerHeight  js/window)]
              :setup  duck-quil/setup
              :update duck-quil/update-state
              :draw   duck-quil/draw-state
              :navigation-2d {}
              :middleware [m/fun-mode m/navigation-2d])


(defn generate-doc-click [& e]
  (get-json-javadoc (@app-state :github-url) ;"https://github.com/weavejester/ring-json-response"
                    (fn [data]
                      (this-as my-this (.log js/console my-this))
                      (.log js/console e)
                      (swap! app-state assoc :javadoc-response (:foo data)))))

(defn test-click [e]
  (.log js/console javadoc-sketch))

(defn main-page []
  [:div
    [:div {:id "overlay"}
      [:h1 (:header-title @app-state)]
      [:div {:class "git-input-box"}
        "Git url"
        [:input {:id "git-url" :type "text"
                 :value (@app-state :github-url)
                 :class (github-url->css-class (@app-state :github-url))
                 :on-change (fn [x] (swap! app-state assoc :github-url (-> x .-target .-value)))}]
        [:a {:href "#" :class "button" :on-click generate-doc-click} "Create"]]
      [:div
        (:javadoc-response @app-state)]
      [:a {:href "#" :class "button" :on-click #(set-quil-pause! (not (@app-state :paused)))} "Pause/resume"]
      [:a {:href "#" :class "button" :on-click test-click} "test"]]])

(reagent/render-component [main-page] (. js/document (getElementById "app")))

(defn windowresize-handler
  [event]
  (do
    (swap! app-state assoc :width (.-innerWidth  js/window))
    (swap! app-state assoc :height (.-innerHeight js/window))))

(defonce on-first-load
  (do
    (println "First load")
    (.addEventListener js/window "resize" windowresize-handler)))

(defn on-js-reload []
  (println ".."))

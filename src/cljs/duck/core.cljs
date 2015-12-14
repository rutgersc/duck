(ns ^:figwheel-always duck.core
  (:require [reagent.core :as reagent]
            [goog.net.XhrIo :as xhr]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [duck.quil :as duck-quil]
            [quil.core :as quil-core :include-macros true]
            [quil.middleware :as m]
            [quil.sketch :as quil-sketch]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (reagent/atom
                     {:header-title "Duck Project!"
                      :javadoc-response ""
                      :github-url "https://github.com/square/retrofit"
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

(defn set-quil-javadoc-state [state]
  (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
    (swap! (quil-core/state-atom) update-in [:doc] (fn [x] state))))

(quil-core/defsketch javadoc-sketch
              :host "javadoc-canvas"
              :size [(.-innerWidth  js/window) (.-innerHeight  js/window)]
              :setup  duck-quil/setup
              :update duck-quil/update-state
              :draw   duck-quil/draw-state
              :navigation-2d {}
              :middleware [m/fun-mode m/navigation-2d])

(defn generate-doc-click [& e]
  (get-json-javadoc (@app-state :github-url)
                    (fn [data]
                      (.log js/console "The javadoc JSON (as a cljs map): " data)
                      (.log js/console "The javadoc JSON: " (clj->js data))
                      (set-quil-javadoc-state data)
                      (swap! app-state assoc :javadoc-response data))))

(defn test-click [e]
  (POST "/test"
        {:foo "test"}
        {:handler (fn [data] (.log js/console data))}))

(defn packages->package-names [packages]
  (let [package-names (clojure.string/join ", " (map #(:name %) packages))]
    (str "Package names: " package-names)))

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
        (packages->package-names (:javadoc-response @app-state))]
      [:a {:href "#" :class "button" :on-click #(set-quil-pause! (not (@app-state :paused)))} "Pause/resume"]
      [:a {:href "#" :class "button" :on-click test-click} "test"]
      [:a {:href "#" :class "button md-trigger md-setperspective" :data-modal "overlay-content"} "fancy overlay with content"]]])

(reagent/render-component [main-page] (. js/document (getElementById "app")))

(defn windowresize-handler
  [event]
  (let [new-width (.-innerWidth  js/window)
        new-height (.-innerHeight js/window)]
    (swap! app-state assoc :width new-width
                           :height new-height)
    (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
      (quil-sketch/size new-width new-height))))

(defonce on-first-load
  (do
    (println "First load")
    (.addEventListener js/window "resize" windowresize-handler)))

(defn on-js-reload []
  (println "..reloaded"))

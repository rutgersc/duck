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
                      :height 500
                      :zoom 0})) ;; this zoom is for handling views (not quil zoom)

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
              :setup  duck-quil/setup2
              ;:update duck-quil/update-state
              :draw   duck-quil/draw-state2
              :navigation-2d {} ;;zoom gets overridden
              :middleware [m/fun-mode m/navigation-2d])

(defn reset-quil-zoom [zoom]
  (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
    (swap! (quil-core/state-atom) assoc-in [:navigation-2d :zoom] zoom)))

(defn generate-doc-click [& e]
  (get-json-javadoc (@app-state :github-url)
                    (fn [data]
                      (reset-quil-zoom 1)
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

; ------------------------------------------------------------
; ZOOM HANDLING
; ------------------------------------------------------------
(defn set-real-zoom [f]
  (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
    (swap! (quil-core/state-atom) update :zoom f)))

(defn get-real-zoom []
  (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
    (:zoom (quil-core/state))))

(defn get-quil-zoom []
  (quil-core/with-sketch (quil-core/get-sketch-by-id "javadoc-canvas")
    (:zoom (:navigation-2d (quil-core/state)))))

(def zoom-lock-time 250) ;; wait-time for next scroll
(def zoom-lock-atom (atom false))

(def zoom-object
  (.getElementById js/document "javadoc-canvas"))

(defn zoom-lock []
  (swap! zoom-lock-atom not))

;; can also load other view here, reset quil-zoom
(defn change-zoom [f zoom]
  (do
    ;; update real zoom
    (set-real-zoom f)
    (let [real-zoom (get-real-zoom)]
      (reset-quil-zoom zoom))))

(defn on-zoom []
  (let [quil-zoom (get-quil-zoom)
        real-zoom (get-real-zoom)]
    ;;(.log js/console real-zoom)
    ;;(.log js/console quil-zoom)
    ;;(case real-zoom
    ;;  0 (if (> quil-zoom 5)
    ;;      (change-zoom inc 1))
    ;;  1 (do (if (> quil-zoom 5)
    ;;          (change-zoom inc 1))
    ;;        (if (< quil-zoom 0.99)
    ;;          (change-zoom dec 4.99)))
    ;;  2 (do (if (< quil-zoom 0.99)
    ;;          (change-zoom dec 4.99)))
    ;;  "default")
    (zoom-lock))) ;; as last remove zoom-lock

(defn zoom-handler
  [event]
  (if (not @zoom-lock-atom)
    (do
      (zoom-lock)
      ;;(if (> (.-deltaY event) 0)
      ;;  (.log js/console "scroll down") ;; zoom out function
      ;;  (.log js/console "scroll up"))  ;; zoom in function
      (.setTimeout js/window on-zoom zoom-lock-time))))

(defonce on-first-load
  (do
    (println "First load")
    (.addEventListener js/window "resize" windowresize-handler)
    (.addEventListener zoom-object "wheel" zoom-handler)))

(defn on-js-reload []
  (println "..reloaded"))

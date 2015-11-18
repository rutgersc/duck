(ns ^:figwheel-always duck.core
  (:require [reagent.core :as reagent]
            [goog.net.XhrIo :as xhr]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :refer-macros [defroute]]))


(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (reagent/atom
                     {:header-title "Hello world!"
                      :javadoc-response ""
                      :github-url"https://github.com/pallix/tikkba"}))

(defn get-json-javadoc [git-url success]
  (POST "/javadoc"
        {:body (str "url=" git-url)
         :response-format :json
         :keywords? true
         :handler success
         :error-handler (fn [err]
                         (.log js/console err))}))

(defn generate-doc-click [& e]
  (get-json-javadoc (@app-state :github-url) ;"https://github.com/weavejester/ring-json-response"
                    (fn [data]
                      (this-as my-this (.log js/console my-this))
                      (.log js/console e)
                      (swap! app-state assoc :javadoc-response (:foo data)))))


(defn github-url? [url]
  (re-matches #"https?://github.com/.*/.*/?" url))

;change this to class names
(defn github-url->color [url]
  (if-not (nil? (github-url? url))
    "green"
    "red"))

(defn main-page []
  [:div
   [:h1 (:header-title @app-state)]
   [:div
    "Git url "
    [:input {:id "git-url" :type "text" :value (@app-state :github-url) :style {:background-color (github-url->color (@app-state :github-url))}
             :on-change (fn [x] (swap! app-state assoc :github-url (-> x .-target .-value) ))}] "  "

    [:span {:class "button" :on-click generate-doc-click} "Create"]]
   [:div
    [:h2 "Server response"]
    (:javadoc-response @app-state)]])





(reagent/render-component [main-page] (. js/document (getElementById "app")))





(defn on-js-reload [])

  ;(get-json-javadoc "https://github.com/weavejester/ring-json-response" (fn [data]
  ;    (println (:foo data))))

  ;(swap! app-state assoc :text "yolo")

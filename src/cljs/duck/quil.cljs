(ns duck.quil
  (:require [reagent.core :as reagent]
            [secretary.core :as secretary :refer-macros [defroute]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  {:color 0
   :angle 0})

(defn update-state [state]
  (let [{:keys [color angle navigation-2d]} state]
    (.log js/console (str "zoom value = "(:zoom navigation-2d)))
    {:color (mod (+ color 0.7) 255)
     :angle (mod (+ angle 0.1) q/TWO-PI)
     :navigation-2d navigation-2d}))

(defn draw-state [state]
  (q/background 240)
  (q/fill (:color state) 255 255)
  (q/line 150 150 300 300)
  (let [angle (:angle state)
        x (* 150 (q/cos angle))
        y (* 150 (q/sin angle))]
    (q/with-translation [(/ (q/width) 2)
                         (/ (q/height) 2)]
      (q/ellipse x y 100 100)
      (q/line x y x+100 y+100))))

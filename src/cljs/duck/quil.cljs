(ns duck.quil
  (:require [reagent.core :as reagent]
            [secretary.core :as secretary :refer-macros [defroute]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))


; ------------------------------------------------------------
; Quil standard methods
; ------------------------------------------------------------

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  {:zoom 0
   :doc nil})

(defn update-state [state]
  (-> state
    (update-in [:zoom] #(int (:zoom (:navigation-2d state)))))) ; Convert zoom value to an int

(defn draw-state [state]
  (q/background 240)
  (q/fill 123 255 255)
  (let [{:keys [zoom doc]} state]
    (when (not (nil? doc))
      (draw-doc doc zoom))))


; ------------------------------------------------------------
; Draw doc methods
; ------------------------------------------------------------

(defn draw-doc [data zoom]
  (let [first-package (first data)] ; Eerst alleen de eerste package testen
    (draw-package first-package 0 zoom)))

(defn draw-package [data root zoom]
  (let [level (- zoom root)
        {:keys [name description classes]} data]
    (q/fill 255 150 150)
    (q/ellipse 100 100 100 100)
    (when (> level 0)
      (q/fill 30 30 30)
      (q/text name 70 100)
      (q/text (subs description 0 30) 70 110))
    (when (> level 1)
      (doseq [c classes] (draw-class c (+ root 1) zoom)))))

(defn draw-class [data root zoom]
  (let [level (- zoom root) ; level voor elke class individueel berekenen vanwege inner classes
        {:keys [name description methods]} data]
    (.log js/console "zoom:" zoom " root:" root " level:" (- zoom root))
    (when (> level 0)
      (q/text name 70 120)); alle x en y zijn nog hardcoded. Alles overlapt nu :|
    (when (> level 1)
      (doseq [m methods] (draw-method m (+ root 1) zoom)))))

(defn draw-method [data root zoom]
  (let [level (- zoom root)
        {:keys [name description]} data]
    (when (> level 0)
      (q/text name 70 150))))

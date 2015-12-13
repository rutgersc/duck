(ns duck.quil
  (:require [reagent.core :as reagent]
            [duck.graph :as g]
            [secretary.core :as secretary :refer-macros [defroute]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))


; ------------------------------------------------------------
; Draw doc methods
; ------------------------------------------------------------
(defn draw-method [data root zoom]
  (let [level (- zoom root)
        {:keys [name description]} data]
    (when (> level 0)
      (q/text name 70 150))))

(defn draw-class [data root zoom]
  (let [level (- zoom root) ; level voor elke class individueel berekenen vanwege inner classes
        {:keys [name description methods]} data]
    (.log js/console "zoom:" zoom " root:" root " level:" (- zoom root))
    (when (> level 0)
      (q/text name 70 120)); alle x en y zijn nog hardcoded. Alles overlapt nu :|
    (when (> level 1)
      (doseq [m methods] (draw-method m (+ root 1) zoom)))))

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


(defn draw-doc [data zoom]
  (let [first-package (first data)] ; Eerst alleen de eerste package testen
    (draw-package first-package 0 zoom)))

;----test----
;(def graph (atom (g/force-graph ["node1" "node2" "node3"] 200 200 0.5)))
;(def graph (atom (g/force-graph ["node1" "node2" "node3" "node4" "node5" "node6" "node7"] 200 200 0.2)))
(def graph (atom (g/force-graph ["node1" "node2" "node3" "node4" "node5" "node6" "node7"
                                 "node11" "node21" "node31" "node41" "node51" "node61" "node71"
                                 "node111" "node211" "node311" "node411" "node511" "node611" "node711"
                                 "node1111" "node2111" "node3111" "node4111" "node5111" "node6111" "node7111"] 200 200 0.2)))

;stop updating the graph when a min totalEnergy has been achieved
;make spring tension variable (based on node/edge count?)
;need to manually normalize node positions with screen with/height. distances in graph are only a few pixels max it seems (maybe extremely low spring tension will work?)
(defn draw-random-nodes []
  (let [result (g/update-graph @graph 0.03)]
    (doseq [[k v] result]
      (q/text k (+ 500 (* 100 (.-x v)))  (+ 500 (* 100 (.-y v)))))))

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
  (draw-random-nodes)
  (let [{:keys [zoom doc]} state]
    (when (not (nil? doc))
      (draw-doc doc zoom))))



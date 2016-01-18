(ns duck.quil
  (:require [reagent.core :as reagent]
            [duck.graph :as g]
            [secretary.core :as secretary :refer-macros [defroute]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

; ------------------------------------------------------------
; Draw doc methods
; ------------------------------------------------------------

(defn determine-text-size [zoom]
  (condp = zoom
    0 18
    1 13
    2 7
    3 6
    4 3
    5))

(defn draw-scaled-text [data x y root]
  (q/text-size (determine-text-size root))
  (q/text data x y))

(defn loop-elements [drawfn startelements root zoom]
  (loop [elements startelements
         newwidth 0
         newheight 0]
    (let [elem (first elements)]
      (if (nil? elem)
        {:w newwidth :h newheight}
        (let [nextx (+ newwidth 0)
              nexty (+ newheight 0)
              elemsize (drawfn elem root zoom nextx nexty)]
          (recur (rest elements)
                 (+ newwidth 10 (:w elemsize))
                 (+ newheight 0))))))) ; (:h classsize))))))))



; ------------------------------------------------------------

(defn draw-method-1 [data root zoom x y]
  (let [{:keys [name]} data]
    (q/fill 30 30 100)
    (draw-scaled-text name (+ x 10) (+ y 10) root)))

(defn draw-method-2 [data root zoom x y]
  (let [{:keys [description]} data]
    (draw-method-1 data root zoom x y)
    (draw-scaled-text (subs description 0 30) (+ x 10) (+ y 30) root)))

(defn draw-method [data root zoom x y]
  (let [level (- zoom root)
        w 100 h 60]
    (q/fill (* level 80) 100 100)
    (q/rect x y w h)
    (cond
      (<= level 0)  (do {:w w :h h})
      (<= level 1)  (do (draw-method-1 data root zoom x y)
                        {:w w :h h})
      (>= level 2)  (do (draw-method-2 data root zoom x y)
                        {:w w :h h}))))

(defn draw-class-1 [data root zoom x y]
  (let [{:keys [name]} data]
    (q/fill 100 10 10)
    (draw-scaled-text name (+ x 10) (+ y 10) root)))

(defn draw-class-2 [data root zoom x y]
  (let [{:keys [description methods]} data]
    (draw-class-1 data root zoom x y)
    (draw-scaled-text (subs description 0 30) (+ x 10) (+ y 30) root)
    (let [innerroot (+ root 1)]
      (q/with-translation [(+ x 5) (+ y 50)]
        (q/scale (/ 1 innerroot))
        (loop-elements draw-method (take 2 methods) innerroot zoom)))))
        ;(draw-method (first methods) next-level zoom 0 0)))))
        ; (doseq [m methods] (draw-method m next-level zoom x y))))))

(defn draw-class [data root zoom x y]
  (let [level (- zoom root) ; level voor elke class individueel berekenen vanwege inner classes
        {:keys [name description methods]} data
        w 100 h 100]
    (q/fill (* level 10) 30 130)
    (cond
      (<= level 0)  (do (q/rect x y w h)
                        {:w w :h h})
      ( = level 1)  (do (q/rect x y w h)
                        (draw-class-1 data root zoom x y)
                        {:w w :h h})
      (>= level 2)  (do (q/rect x y w h)
                        (draw-class-2 data root zoom x y)
                        {:w w :h h}))))

(defn draw-package [data root zoom x y]
  (let [level (- zoom root),
        {:keys [name description classes]} data
        fixedwidth 300
        fixedheight 300]
    (q/fill 255 150 150)
    (if (zero? (count classes))
      (q/rect x y 150 80)
      (q/rect x y fixedwidth fixedheight))
    ;(when (> level 0)
    (q/fill 200 30 30)
    (let [name-width (count name)]
      (draw-scaled-text name (+ x name-width) (+ y 10) root))
    (when (> level 1)
      (draw-scaled-text (subs description 0 30) (+ x 10) (+ y 30) root)
      (let [innerroot (+ root 1)]
        (q/with-translation [x (+ y 50)]
          (q/scale (/ 1 innerroot))
          (loop-elements draw-class classes innerroot zoom))))
    {:w fixedwidth :h fixedheight}))

(defn draw-doc [data zoom]
  (let [first-package (first data)]
    (loop-elements draw-package data 1 zoom)))

(defn draw-example-method [zoom]
  (let [data {:name "testName" :description "cyka blyat haaieeeeee"}
        x 150 y 250 root 1]
    (draw-method data root zoom x y)
    (q/with-translation [x y]
      (q/scale (/ 1 (+ root 1)))
      (draw-method data (+ root 1) zoom 100 10))
    (q/with-translation [x y]
      (q/scale (/ 1 (+ root 2)))
      (draw-method data (+ root 2) zoom 50 50))))


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
  (q/color-mode :rgb)
  {:zoom 0
   :doc nil})

(defn update-state [state]
  (-> state
    (update-in [:zoom] #(int (:zoom (:navigation-2d state)))))) ; Convert zoom value to an int

(defn draw-state [state]
  ; (.log js/console (:zoom (:navigation-2d state)))
  (q/background 0 0)
  (q/fill 123 255 255)
  ;(draw-random-nodes)
  ; (draw-example-method (:zoom state))
  (let [{:keys [zoom doc]} state]
    (when (not (nil? doc))
      (draw-doc doc zoom))))

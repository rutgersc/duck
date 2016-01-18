(ns duck.quil
  (:require [reagent.core :as reagent]
            [duck.graph :as g]
            [secretary.core :as secretary :refer-macros [defroute]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [clojure.walk :as w]))

; ------------------------------------------------------------
; Draw doc methods
; ------------------------------------------------------------

(defn determine-text-size [zoom]
  (condp = zoom
    0 18
    1 14
    2 7
    3 4
    4 3
    5))

(defn draw-method [data root zoom x y]
  (let [level (- zoom root)
        {:keys [name description]} data
        w 100 h 60
        w_scaled (quot w root) h_scaled (quot h root)
        text_size_scaled (determine-text-size root)]
    (q/fill (* level 80) 100 100)
    (q/rect x y w_scaled h_scaled)
    (cond
      (<= level 0)  (do
                      {:w w_scaled :h h_scaled})
      (>= level 1)  (let [name_x (+ x (quot 10 root))
                          name_y (+ y (quot 15 root))]
                      (q/fill 30 30 100)
                      (q/text-size text_size_scaled)
                      (q/text name name_x name_y)
                      {:w w_scaled :h h_scaled})
      :else (.log js/console "ERROR!!" level))))

(defn draw-class [data root zoom x y]
  (let [level (- zoom root) ; level voor elke class individueel berekenen vanwege inner classes
        {:keys [name description methods]} data
        w 100 h 60]
    (q/fill 10 50 100)
    (q/rect x y w h)
    (when (> level 0)
      (q/fill 100 10 10)
      (q/text-size (determine-text-size zoom))
      (q/text name (+ x 10) (+ y 10))); alle x en y zijn nog hardcoded. Alles overlapt nu :|
    ; (when (> level 1)
    ;   (doseq [m methods] (draw-method m (+ root 1) zoom)))))
    {:w w :h h}))

(defn draw-package [data root zoom x y]
  (let [level (- zoom root),
        {:keys [name description classes]} data]
    (q/fill 255 150 150)
    (q/rect x y 400 400)
    (when (> level 0)
      (q/fill 200 30 30)
      (let [name-width (count name)]
        (q/text name (+ x name-width) (+ y -10))))
    (when (> level 1)
      (q/text (subs description 0 30) (+ x -40) (+ y 20))
      (doseq [c classes] (draw-class c (+ root 1) zoom x y)))))

(defn draw-doc [data zoom]
  (let [xys (-> (meta data) :graph (g/update-graph 0.02))]
    (doseq [package data]
      (let [x (* 200 (.-x (xys package)))
            y (* 200 (.-y (xys package)))]
;(println y)
        (draw-package package 0 zoom x y)))))
    ;(draw-package first-package 1 (+ zoom 1) 0 500)))

(defn draw-example-method [zoom]
  (let [data {:name "testName"
              :description "cyka blyat haaieeeeee"}
        x 150 y 250]
    (draw-method data 1 zoom x y)
    (draw-method data 2 zoom (+ x 10) (+ y 20))
    (draw-method data 3 zoom (+ x 30) (+ y 30))))

;----test----
(def graph (atom (g/force-graph [{:test "ok"} {:test "ok2"} ] 200 200 0.5)))
;(def graph (atom (g/force-graph ["node1" "node2" "node3" "node4" "node5" "node6" "node7"] 200 200 0.2)))
;(def graph (atom (g/force-graph ["node1" "node2" "node3" "node4" "node5" "node6" "node7"
;                                 "node11" "node21" "node31" "node41" "node51" "node61" "node71"
;                                 "node111" "node211" "node311" "node411" "node511" "node611" "node711"
;                                 "node1111" "node2111" "node3111" "node4111" "node5111" "node6111" "node7111"] 200 200 0.2)))

;stop updating the graph when a min totalEnergy has been achieved
;make spring tension variable (based on node/edge count?)
;need to manually normalize node positions with screen with/height. distances in graph are only a few pixels max it seems (maybe extremely low spring tension will work?)
(defn draw-random-nodes []
  (let [result (g/update-graph @graph 0.03)]
    (doseq [[k v] result]
      (q/text k (+ 500 (* 100 (.-x v)))  (+ 500 (* 100 (.-y v)))))))

;assign graph to toplevel (packages) and (recursively) classess and other packages
(defn assign-graphs [data]
  (with-meta (w/walk
           #(if (or (= "class" (:type %)) (= "package" (:type %)))
              (with-meta % {:graph (g/force-graph % 200 200 0.5)})
              %)
           identity
           data)
    {:graph (g/force-graph data 200 200 0.3)}))
  ;(println data))

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
    (update-in [:zoom] #(int (:zoom (:navigation-2d state))))
    (update-in [:doc]
               #(if (and (not (nil? %)) (nil? (:graph (meta %)))) ;when not initiated yet
                 (assign-graphs %)
                 %))
      ))  ; Convert zoom value to an int

(defn draw-state [state]
  ;(.log js/console (:zoom (:navigation-2d state)))
  (q/background 0 0)
  (q/fill 123 255 255)
  (draw-random-nodes)
  (draw-example-method (:zoom state))
   (let [{:keys [zoom doc]} state]
     (when (not (nil? doc))
       (draw-doc doc zoom))))

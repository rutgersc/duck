(ns duck.quil
  (:require [reagent.core :as reagent]
            [duck.graph :as g]
            [secretary.core :as secretary :refer-macros [defroute]]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [domina :as dom]
            [domina.xpath :refer [xpath]]))

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

; unlimited classes in a row (for now)
; 6 classes in a row
; 3 methods in a row
(def draw-widths {:package 355
                  :class 115
                  :method 100})

(def method-height 60)

(defn get-size-for-package [data]
  (let [{:keys [name description classes]} data]
    ;(.log js/console data)
    (let [classes-amount (count classes)
          highest-methods-amount (apply max (map count classes))]
      (if (> classes-amount 0)
        {:width (:package draw-widths) :height (* (+ 1 (quot classes-amount 6)) (* method-height (+ (quot highest-methods-amount 3) 1)))}
        {:width (:package draw-widths) :height 300}))))

(defn get-size-for-class [data]
  (let [{:keys [name description methods]} data]
    ;(.log js/console data)
    (let [methods-amount (count methods)]
      ;(.log js/console (str name ": " methods-amount))
      {:width (:class draw-widths) :height 130})));:height (* method-height (+ (quot methods-amount 3) 1))})))

(defn get-size-for-method [data]
  {:width (:method draw-widths) :height method-height})

(defn get-size-for-package-and-classes-OLD [type data]
  (let [{:keys [name description classes]} data]
    ;(.log js/console data)
    (let [classes-amount (count classes)
          highest-methods-amount (apply max (map count classes))]
      ;(.log js/console (str type ": " highest-methods-amount))
      (case type
        "package" {:width (:package draw-widths) :height (* (+ 1 (quot classes-amount 6)) (* method-height (+ (quot highest-methods-amount 3) 1)))}
        "class"   {:width (:class draw-widths)   :height (* method-height (+ (quot highest-methods-amount 3) 1))}
        "method"  {:width (:method draw-widths)  :height method-height}
        "default"))))


;; ik kan hier width, height mss wel toevoegen aan drawfn
(defn loop-elements [drawfn startelements root zoom sizefn fitting]
  (loop [elements startelements
         index 0]
    (let [elem (first elements)]
      (let [size (sizefn elem)]
        (if (nil? elem)
          "elem is nil lol"
          (let [nextx (+ 10 (* (rem index fitting) (:width size)))
                nexty (+ 10 (* (quot index fitting) (:height size)))]
            (drawfn elem root zoom nextx nexty size)
            (recur (rest elements)
                   (inc index)))))))) ; (:h classsize))))))))

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

(defn draw-class-1 [data root zoom x y size]
  (let [{:keys [name]} data]
    (q/fill 100 10 10)
    (draw-scaled-text name (+ x 10) (+ y 10) root)))

(defn draw-class-2 [data root zoom x y size]
  (let [{:keys [description methods]} data]
    (draw-class-1 data root zoom x y size)
    (draw-scaled-text (subs description 0 30) (+ x 10) (+ y 30) root)
    (let [innerroot (+ root 1)]
      (q/with-translation [(+ x 5) (+ y 50)]
        (q/scale (/ 1 innerroot))
        (loop-elements draw-method (take 8 methods) innerroot zoom get-size-for-method 3)))))
        ;(draw-method (first methods) next-level zoom 0 0)))))
        ; (doseq [m methods] (draw-method m next-level zoom x y))))))

(defn draw-class [data root zoom x y size]
  (let [level (- zoom root) ; level voor elke class individueel berekenen vanwege inner classes
        {:keys [name description methods]} data
        w (:width size)
        h (:height size)]
    ;(.log js/console size)
    (q/fill (* level 10) 30 130)
    (cond
      (<= level 0)  (do (q/rect x y w h))
      ( = level 1)  (do (q/rect x y w h)
                        (draw-class-1 data root zoom x y size))
      (>= level 2)  (do (q/rect x y w h)
                        (draw-class-2 data root zoom x y size)))))

(defn draw-package [data root zoom x y size]
  (let [level (- zoom root),
        {:keys [name description classes]} data
        fixedwidth (:width size)
        fixedheight (:height size)]
    (q/fill 255 150 150)
    (if (zero? (count classes))
      (q/rect x y 150 80)
      (q/rect x y fixedwidth fixedheight))
    (when (> level 0)
      (dom/set-styles! (xpath "//div[@id='app']/*") {:visibility "visible"}))
    (q/fill 200 30 30)
    (let [name-width (count name)]
      ;(.log js/console name)
      (draw-scaled-text name (+ x name-width) (+ y 10) root))
    (when (> level 1)
      (dom/set-styles! (xpath "//div[@id='app']/*") {:visibility "hidden"})
      (draw-scaled-text (subs description 0 30) (+ x 10) (+ y 30) root)
      (let [innerroot (+ root 1)]
        (q/with-translation [x (+ y 50)]
          (q/scale (/ 1 innerroot))
          (loop-elements draw-class classes innerroot zoom get-size-for-class 6))))))

(defn draw-doc [data zoom]
  (let [first-package (first data)]
    (q/translate (- (/ (q/width) 2) (/ (* (:package draw-widths) (count data)) (count data))) (/ (q/height) 2)) ;; start some where near center
    ;(.log js/console (:height size))
    (loop-elements draw-package data 1 zoom get-size-for-package 100)))

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

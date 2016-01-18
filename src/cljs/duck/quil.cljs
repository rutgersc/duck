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
; Minardi's draw documentation methods
;
; [0] = alleen naam van packages (max zoom level)
; .. die zijn maybe connected, tekenen met Springy
; ------------------------------------------------------------
(defn get-real-zoom []
  (:zoom (q/state)))

(def colors
  {:orange [255 153 51]
   :black [0 0 0]})

(def arc-length (atom 0))
(defn draw-package-name [c r w h x]
  (let [w (q/text-width c)]
    (reset! arc-length (+ @arc-length (/ w 2)))
    (let [theta (+ q/PI (/ @arc-length r))]
        (q/push-matrix)
        (q/translate (* r (q/cos theta)) (* r (q/sin theta)))
        (q/rotate (+ theta (/ q/PI 2)))
        (q/fill 0)
        (q/text c 0 0)
        (q/pop-matrix))
    (reset! arc-length (+ @arc-length (/ w 2)))))

(defn draw-package2 [data x amount]
  (let [r 100
        w 40
        h 40
        {:keys [name description]} data
        zoom-level (:zoom (:navigation-2d (q/state)))]
    (if (= x 0)
        (q/translate (- (/ (q/width) 2) (* amount r)) (/ (q/height) 2))
        (q/translate x 0))
    (q/fill 255 153 51)
    (q/no-stroke)
    (q/ellipse 0 0 (* (+ r 33) 2) (* (+ r 33) 2))
    (q/fill 200 255 255)
    (q/ellipse 0 0 (* (- r 2) 2) (* (- r 2) 2))
    (q/fill  0 0)
    (q/text-font (q/create-font "Georgia" w true))
    (q/text-align :center)
    (doseq [c name]
      (draw-package-name c r w h x))
    ;; extras
    (q/text-font (q/create-font "Georgia" 4 true))
    (if (< zoom-level 1.5)
      (dom/set-styles! (xpath "//div[@id='app']/*") {:visibility "visible"}))
    (if (> zoom-level 1.5)
      (dom/set-styles! (xpath "//div[@id='app']/*") {:visibility "hidden"}))
    (if (> zoom-level 2.5) ;; draw classnames & interfaces inside package
      (q/text "classes:" 0 -90)))
    (reset! arc-length 0))

(defn draw-doc2 [data]
  (let [amount (count data)]
    (doseq [x (range 0 amount 1)]
      (draw-package2 (get data x) (* x 400) amount))))
  ;;(case (get-real-zoom)
  ;;  0 (let [amount (count data)]
  ;;      (doseq [x (range 0 amount 1)]
  ;;        (draw-package2 (get data x) (* x 400) amount)))
  ;;  "default"))

    ;;(doall (for [x (range 0 amount 1)]
             ;;(draw-package2 (get data x) (* x 400) amount)))))

(defn setup2 []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  {:zoom 0
   :doc nil})

(defn draw-state2 [state]
  (q/background 0 0)
  ; draw zoom level 0
  (let [{:keys [doc]} state]
    (when (not (nil? doc))
      (draw-doc2 doc))))
; ------------------------------------------------------------
; Quil standard methods
; ------------------------------------------------------------

(defn setup []
  (q/frame-rate 30)
  {:zoom 0
   :doc nil})

(defn update-state [state]
  (-> state
    (update-in [:zoom] #(int (:zoom (:navigation-2d state)))))) ; Convert zoom value to an int

(defn draw-state [state]
  (q/background 0 0)
  (q/fill 123 255 255)
  (draw-random-nodes)
  (let [{:keys [zoom doc]} state]
    (when (not (nil? doc))
      (draw-doc doc zoom))))



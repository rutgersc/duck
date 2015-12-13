(ns duck.graph)

(def s js/Springy)


;(constantly-dropping-range 5) => '((1 2 3 4) (2 3 4) (3 4))
;(constantly-dropping-range 3) => '((1 2) (3))
(defn- constantly-dropping-range [r]
  (map #(range % r) (range 1 r)))

(defn- create-graph2 [xs]
  (let [graph (new s.Graph)
        nodes (doall (map #(.call graph.newNode graph %)     xs))
        nths-list (constantly-dropping-range (count nodes))
        edges (doall (mapcat (fn [node nths]
                              (map #(.call graph.newEdge graph (nth nodes %) node) nths))
                             nodes
                             nths-list))]
    graph))

;(force-directed-graph graph 400 400 0.5)
(defn- force-graph- [graph x y z]
  (s.Layout.ForceDirected. graph x y z))

(defn- update-internal [force-graph ticks]
  (.call force-graph.tick force-graph ticks))

(defn- get-node-map [force-graph] (reduce
                                    #(assoc %1  (aget %2 "data") (.-p (aget force-graph.nodePoints (aget %2 "id"))))
                                    {}
                                    force-graph.graph.nodes))

;------------------------
;----- global stuff -----
;------------------------
; x: Spring stiffness
; y: Node repulsion
; z: Damping
;(force-graph ["a" "b" "c"] 400 400 0.5)
(defn force-graph [nodes x y z] (-> (create-graph2 nodes)
                                     (force-graph- x y z)))


;returns format: {initially-given-node #js {x 0 y 0}}
;getting x val example:  (.-x (get (update-graph l 0.02) "a"))
(defn update-graph [force-graph ticks]
  (update-internal force-graph ticks)
  (get-node-map force-graph))


  ;(comment (println (.-nodePoints force-graph))))

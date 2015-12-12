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
(defn- force-graph [graph x y z]
  (s.Layout.ForceDirected. graph x y z))

(defn- update-internal [force-graph ticks]
  (.call force-graph.tick force-graph ticks))


;------------------------
;----- global stuff -----
;------------------------
;(create-graph ["a" "b" "c"] 400 400 0.5)
(defn create-graph [nodes x y z] (-> (create-graph2 nodes)
                                     (force-graph x y z)))


;todo: return map like this: {class1 [x y], class2 [x y], etc}
(defn update-graph [force-graph ticks]
  (update-internal force-graph ticks))
  ;(println (.call force-graph.totalEnergy force-graph)))

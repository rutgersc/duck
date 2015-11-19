(ns duck.doclet
  (:gen-class
      :extends com.sun.javadoc.Doclet
      :name "DuckDoclet"
      :main false
      :methods [^:static [start [com.sun.javadoc.RootDoc] boolean]]))

(defn -start [root]
  (let [packages (.specifiedPackages root)
        all-classes (mapcat #(.allClasses %) packages)
        classes     (mapcat #(.ordinaryClasses %) packages)
        interfaces  (mapcat #(.interfaces %) packages)]

    (println "All packages:" (count packages))
    (doseq [p packages] (println "  "(.name p)))

    (println "All classes:")
    (doseq [c all-classes] (println "  " (.name c)))

    (println "Classes only:")
    (doseq [c classes] (println "  " (.name c)))

    (println "Interfaces only:")
    (doseq [c interfaces] (println "  " (.name c))))
  true)

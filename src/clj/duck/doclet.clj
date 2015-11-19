(ns duck.doclet
  (:gen-class
      :extends com.sun.javadoc.Doclet
      :name "DuckDoclet"
      :main false
      :methods [^:static [start [com.sun.javadoc.RootDoc] boolean]]))

(defn print-package-name [package]
  (println " " (.name package)))

(defn all-classes [packages]
  (mapcat (fn [p] (.ordinaryClasses p)) packages))

(defn -start [root]
  (let [packages (-> root .specifiedPackages)]
    (println "All packages:")
    (doall (map print-package-name packages))
    (println "All classes:")
    (let [classes (all-classes packages)]
      (doall (map (fn [c] (println " " (.name c))) classes))))
  true)

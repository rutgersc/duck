(ns duck.doclet
  (:gen-class
      :extends com.sun.javadoc.Doclet
      :name "DuckDoclet"
      :main false
      :methods [^:static [start [com.sun.javadoc.RootDoc] boolean]]))

(defn -start [root]
    (println "Hello, javadoc!!")
    true)

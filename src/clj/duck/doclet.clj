(ns duck.doclet
  (:gen-class
     :name "TestClass"
     :extends [com.sun.javadoc.Doclet])))

(defn -start [root]
    true)

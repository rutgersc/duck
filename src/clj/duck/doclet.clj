(ns duck.doclet
  (:require
    [cheshire.core :as json])
  (:gen-class
      :extends com.sun.javadoc.Doclet
      :name "DuckDoclet"
      :main false
      :methods [^:static [start [com.sun.javadoc.RootDoc] boolean]]))

; --------

(defn print-com_sun_javadoc_Doc-names
  "Items in the array MUST have .name function (implemented from com.sun.javadoc.Doc)"
  [name array]
  (println name)
  (doseq [v array] (println "  " (.name v))))

; --------

(defn keep-classes [list]
  (filter #(not (.isClass %)) list))

(defn keep-interfaces [list]
  (filter #(not (.isInterface %)) list))

; --------

(defn docname [v]
  (.qualifiedName v))

(defn process-tag [t]
  (.toString t))

(defn process-type [t]
  (.toString t))

(defn process-type-variable [t]
  { :type-name (.typeName t)
    :simple-type-name (.simpleTypeName t)
    :qualified-type-name (.qualifiedTypeName t)
    :element-type (process-type (.getElementType t))
    :bounds (map process-type (.bounds t))})

(defn process-param-tag [p]
  { :name (.name p)
    :text (.text p)
    :parameter-name (.parameterName p)
    :parameter-comment (.parameterComment p)})

(defn process-field [f]
  { :name (.name f)
    :description (.commentText f)
    :type (process-type (.type f))
    :tags (map process-tag (.tags f))})

(defn process-parameter [p]
  { :name (.name p)
    :type (process-type (.type p))
    :type-name (.typeName p)})

(defn process-constructor [c]
  { :name (.name c)
    :description (.commentText c)
    :parameters (map process-parameter (.parameters c))})

(defn process-method [m]
  { :name (.name m)
    :description (.commentText m)
    :return-type (process-type (.returnType m))
    :parameters (map process-parameter (.parameters m))})

(defn process-class [c]
  { :name              (.name c)
    :description       (.commentText c)
    :is-abstract       (.isAbstract c)
    :fields            (map process-field (.fields c))
    :constructors      (map process-constructor (.constructors c))
    :methods           (map process-method (.methods c))
    :super-class       (if-let [sc (.superclass c)] (docname sc)) ; TODO: Use superclassType, https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/com/sun/javadoc/ClassDoc.html#superclassType--
    :implemented-interfaces (map docname (.interfaces c))})
;    :nested-classes    (keep-classes (.innerClasses c))
;    :nested-interfaces (keep-interfaces (.innerClasses c))})
;    :known-subclasses  ???

(defn process-interface [i]
  { :name              (.name i)
    :description       (.commentText i)
    :type-parameters   (map process-type-variable (.typeParameters i)) ; werkt niet ?
    :type-param-tags   (map process-param-tag (.typeParamTags i)) ; werkt niet ?
    :fields            (map process-field (.fields i))
    :methods           (map process-method (.methods i))
    :implemented-interfaces (map docname (.interfaces i))})
;    :nested-classes    (keep-classes (.innerClasses i))
;    :nested-interfaces (keep-interfaces (.innerClasses i))})
;    :known-subinterfaces  ???

(defn contains-qualified-name? [class coll]
  (some #(= (.qualifiedName class) (.qualifiedName %)) coll))

(defn process-package [p]
  (let [all-classes (.allClasses p)
        all-inners  (mapcat #(.innerClasses %) all-classes)
        classes     (remove #(contains-qualified-name? % all-inners) all-classes)]
    (println "\n\n-------------------------------------------")
    (println "\nPackage: " (.name p))
    (print-com_sun_javadoc_Doc-names "All classes/interfaces" all-classes)
    (print-com_sun_javadoc_Doc-names "\nInners" all-inners)
    (print-com_sun_javadoc_Doc-names "\nFiltered" classes)
    { :name (.name p)
      :description (.commentText p)
      :interfaces (map process-interface (.interfaces p))
      :classes (map process-class (.ordinaryClasses p))}))


(defn -start [root]
  (let [packages (.specifiedPackages root)
        processed (map process-package packages)]
    (println "\n")
    (spit "flubber.txt" (json/generate-string processed {:pretty true})))
  true)

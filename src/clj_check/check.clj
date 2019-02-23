(ns clj-check.check
  (:require
   [bultitude.core :as bultitude]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn- file-for [ns] (-> ns name (str/replace \- \_) (str/replace \. \/)))

(defn check [source-paths]
  (let [source-files (->> (or (seq source-paths) ["src"])
                          (map io/file))
        nses         (bultitude/namespaces-on-classpath
                      :classpath source-files
                      :ignore-unreadable? false)]
    (let [failures (atom 0)]
      (doseq [ns nses]
        (binding [*out* *err*]
          (println "Compiling namespace" ns))
        (try
          (binding [*warn-on-reflection* true]
            (load (file-for ns)))
          (catch ExceptionInInitializerError e
            (swap! failures inc)
            (.printStackTrace e))))
      (if-not (zero? @failures)
        (System/exit @failures)))))

(defn -main [& source-paths]
  (check source-paths))

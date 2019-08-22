(ns wb-extract.core
  (:require
   [datomic.api :as d]
   [environ.core :as environ]
   [mount.core :as mount]
   [clojure.data.csv :as csv]
   [clojure.java.io :as io])
  (:gen-class))

(defn datomic-uri []
  (environ/env :wb-db-uri))

(defn- connect []
  (let [db-uri (datomic-uri)]
    (do
      (println (format "Using Datomic: %s" db-uri))
      (d/connect db-uri))))

(defn- disconnect [conn]
  (d/release conn))

(mount/defstate datomic-conn
  :start (connect)
  :stop (disconnect datomic-conn))

(defn dump-interactions [db]
  (let [data (->> (d/q '[:find ?ti ?en ?eti ?nn ?nti ?iid ?pid
                         :in $
                         :where
                         [?i :interaction/id ?iid]
                         [?i :interaction/paper ?p]
                         [?i :interaction/type ?t]
                         [?i :interaction/interactor-overlapping-gene ?ie]
                         [?i :interaction/interactor-overlapping-gene ?in]
                         [(not= ?ie ?in)]
                         [?in :interaction.interactor-overlapping-gene/gene ?n]
                         [?ie :interaction.interactor-overlapping-gene/gene ?e]
                         [?n :gene/public-name ?nn]
                         [?e :gene/public-name ?en]
                         [(< ?en ?nn)]
                         [?in :interactor-info/interactor-type ?nt]
                         [?nt :db/ident ?nti]
                         [?ie :interactor-info/interactor-type ?et]
                         [?et :db/ident ?eti]
                         [?e :gene/id ?gid]
                         [?t :db/ident ?ti]
                         [?p :paper/id ?pid]
                         ]
                       db)
                  (sort-by (fn [[_ gene1 _ gene2 _ _]]
                             (str gene1 ":" gene2)))
                  (cons ["interactionType" "gene1" "gene1Type" "gene2" "gene2Type" "interactionID" "citation"]))]
    (with-open [writer (io/writer "output/out-file.csv")]
      (csv/write-csv writer data))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (mount/start)
  (dump-interactions (d/db datomic-conn))
  (println "Hello, World!"))

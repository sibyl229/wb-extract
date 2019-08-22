(defproject wb-extract "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [environ "1.1.0"]
                 [mount "0.1.11"]]
  :profiles
  {:datomic-free
   {:dependencies [[com.datomic/datomic-free "0.9.5703"
                    :exclusions [joda-time]]]}
   :datomic-pro
   {:dependencies [[com.datomic/datomic-pro "0.9.5703"
                    :exclusions [joda-time]]]}
   :ddb
   {:dependencies
    [[com.amazonaws/aws-java-sdk-dynamodb "1.11.82"]]}
   :dev [:datomic-pro
         :ddb
         {:aliases
          {"code-qa"
           ["do"
            ["eastwood"]
            "test"]}
          :dependencies [[org.clojure/tools.trace "0.7.9"]]
          :source-paths ["dev"]
          :jvm-opts ["-Xmx1G"]
          :env
          {:wb-db-uri "datomic:ddb://us-east-1/WS271/wormbase"
           :swagger-validator-url "http://localhost:8002"}
          :plugins
          [[jonase/eastwood "0.2.3"
            :exclusions [org.clojure/clojure]]
           [lein-ancient "0.6.8"]
           [lein-bikeshed "0.3.0"]
           [lein-ns-dep-graph "0.1.0-SNAPSHOT"]
           [venantius/yagni "0.1.4"]
           [com.jakemccrary/lein-test-refresh "0.17.0"]]}]
      :test
   {:resource-paths ["test/resources"]}}
  :javac-options ["-target" "1.8" "-source" "1.8"]
  :jvm-opts ["-Xmx28G"
             ;; same GC options as the transactor,
             ;; should minimize long pauses.
             "-XX:+UseG1GC" "-XX:MaxGCPauseMillis=50"
             "-Ddatomic.txTimeoutMsec=1000000"]
  :main ^:skip-aot wb-extract.core
  :target-path "target/%s")

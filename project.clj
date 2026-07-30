(defproject self-destruct "0.1.0-SNAPSHOT"
  :description "run your own self destucting note service"
  :url "http://github.com/chadhs/self-destruct"
  :min-lein-version "2.11.0"
  :dependencies [[org.clojure/clojure "1.12.5"]
                 ;;; core
                 [ring/ring "1.15.5"]
                 [compojure "1.7.2"]
                 ;;; environment
                 [environ "1.2.0"]
                 [org.clojure/tools.cli "1.1.230"]
                 ;;; database
                 [com.layerware/hugsql "0.5.3"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.7.7"]
                 [migratus "1.6.7"]
                 ;;; logging
                 [com.taoensso/timbre "6.8.0"]
                 [raven-clj "1.7.0"] ; timbre sentry support
                 ;;; security
                 [buddy/buddy-core "1.12.0-430"]
                 ;;; ui
                 [hiccup/hiccup "2.0.0"]
                 [garden "1.3.10"]
                 ;;; middleware
                 [ring/ring-defaults "0.7.1"]
                 ;;; data
                 [cheshire "5.13.0"]
                 ;;; scheduling
                 [tea-time "1.0.1"]
                 ;;; hosted assests
                 [ring-webjars "0.3.1"]
                 [org.webjars/font-awesome "6.7.2"]]


  :plugins [[lein-environ "1.2.0"]
            [lein-ring "0.12.6"]
            [migratus-lein "0.7.3"]
            [lein-garden "0.3.0"]
            [lein-pdo "0.1.1"]]


  :ring {:init self-destruct.core/init
         :handler self-destruct.core/app
         :port 8000
         :auto-refresh? true}


  :garden {:builds [{:source-paths ["src"]
                     :id           "style"
                     :stylesheet   self-destruct.css/style
                     :compiler     {:output-to     "resources/public/css/style.css"
                                    :pretty-print? true}}]}


  :clean-targets ^{:protect false} ["resources/public/css"]


  :migratus {:store :database
             :migration-dir "migrations"
             :db {:jdbcUrl ~(or (System/getenv "DATABASE_URL")
                                "jdbc:postgresql://localhost:5432/self-destruct-dev?user=selfdestruct&password=selfdestruct")}}


  :profiles {:uberjar {:aot :all
                       :env {:secure-defaults "true"}}
             :dev  [:project/dev  :profiles/dev]
             :test [:project/test :profiles/test]
             :prod [:project/prod :profiles/prod ]
             ;; only edit :profiles/* in profiles.clj
             :profiles/dev  {}
             :profiles/test {}
             :profiles/prod {}
             :project/dev {:main self-destruct.core/-dev-main
                           :dependencies [[ring/ring-mock "0.6.2"]]}
             :project/test {:dependencies [[ring/ring-mock "0.6.2"]]}
             :project/prod {}}


  :main self-destruct.core


  :uberjar-name "self-destruct.jar"


  :prep-tasks ["clean" ["garden" "once"] "compile"]


  ;; Use the app CLI for migrations so we always run migratus 1.6+ / next.jdbc
  ;; from project deps (migratus-lein still pins an older migratus).
  :aliases {"migrate" ["run" "--" "--migrate"]
            "dev"     ["pdo" ["garden" "auto"] ["ring" "server-headless"]]}


  )

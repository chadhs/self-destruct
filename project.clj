(defproject self-destruct "0.1.0-SNAPSHOT"
  :description "run your own self destucting note service"
  :url "http://github.com/chadhs/self-destruct"
  :min-lein-version "2.8.3"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ;;; core
                 [ring "1.7.1"]
                 [compojure "1.6.1"]
                 ;;; environment
                 [environ "1.1.0"]
                 ;;; database
                 [com.layerware/hugsql "0.4.9"]
                 [org.postgresql/postgresql "42.2.5"]
                 [migratus "1.2.3"]
                 ;;; logging
                 [com.taoensso/timbre "4.10.0"]
                 [raven-clj "1.5.2"] ; timbre sentry support
                 ;;; security
                 [buddy "2.0.0"]
                 ;;; ui
                 [hiccup "1.0.5"]
                 [garden "1.3.9"]
                 ;;; middleware
                 [ring/ring-defaults "0.3.2"]
                 ;;; hosted assests
                 [ring-webjars "0.2.0"]
                 [org.webjars/font-awesome "5.8.2"]]


  :plugins [[lein-environ "1.1.0"]
            [lein-ring "0.12.5"]
            [migratus-lein "0.7.2"]
            [lein-garden "0.3.0"]
            [lein-pdo "0.1.1"]]


  :ring {:handler self-destruct.core/app
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
             :db ~(get (System/getenv) "DATABASE_URL")}


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
                           :dependencies [[ring/ring-mock "0.3.2"]]}
             :project/test {:dependencies[[ring/ring-mock "0.3.2"]]}
             :project/prod {}}


  :main self-destruct.core


  :uberjar-name "self-destruct.jar"


  :prep-tasks ["clean" ["garden" "once"] "compile"]


  :aliases {"migrate" ["migratus" "migrate"]
            "dev"     ["pdo" ["garden" "auto"] ["ring" "server"]]}


  )

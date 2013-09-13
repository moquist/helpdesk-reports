(defproject helpdesk-reports "0.1.0"
  :description "Moodle Help Desk Block Reports"
  :url "https://github.com/vlacs/helpdesk-reports"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [korma "0.3.0-RC5"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [environ "0.4.0"]]
  :plugins [[lein-ring "0.8.5"]
            [lein-environ "0.4.0"]]
  :ring {:handler helpdesk-reports.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})

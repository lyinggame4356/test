(defproject macroscale/system.check "0.1.0"
  :description "Plug simulation capability in to test.check"
  :url "https://macroscale.net/open-source"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/test.check "0.9.0"]
                 [org.clojure/math.combinatorics "0.1.3"]
                 [org.clojure/core.match "0.3.0-alpha4"]]
  :deploy-repositories
  [["releases"
    {:url "http://repo.macroscale.net/artifactory/libs-release-local" }]
   ["snapshots"
    {:url "http://repo.macroscale.net/artifactory/libs-snapshot-local" }]])

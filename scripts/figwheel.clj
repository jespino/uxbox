(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra]
         '[cljs.tagged-literals])

(alter-var-root #'cljs.tagged-literals/*cljs-data-readers*
                assoc 'ux/tr (fn [v] `(uxbox.locales/tr ~v)))

(ra/start-figwheel!
  {:figwheel-options {:css-dirs ["resources/public/css"]}
   :build-ids ["dev" "worker-align"]
   :all-builds
   [{:id "dev"
     :figwheel {:on-jsload "uxbox.ui/init"}
     :source-paths ["src" "vendor"]
     :compiler {:main 'uxbox.core
                :asset-path "js"
                :parallel-build false
                :optimizations :none
                :closure-defines {"uxbox.repo.core.url"
                                  "https://test.uxbox.io/api"}
                :warnings {:ns-var-clash false}
                :pretty-print true
                :language-in  :ecmascript6
                :language-out :ecmascript5
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js"
                :verbose true}}

    {:id "worker-align"
     :source-paths ["src" "vendor"]
     :compiler {:main 'uxbox.workers.align
                :asset-path "js"
                :parallel-build false
                :optimizations :simple
                :warnings {:ns-var-clash false}
                :pretty-print true
                :static-fns true
                :language-in  :ecmascript6
                :language-out :ecmascript5
                :output-to "resources/public/js/worker-align.js"
                ;; :source-map "resources/public/js/worker-align.js.map"
                ;; :output-dir "resources/public/js/out-worker"
                :verbose true}}]})

(ra/cljs-repl)

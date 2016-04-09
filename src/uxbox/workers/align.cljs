;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.workers.align
  "Workspace aligment indexes worker."
  (:require [beicon.core :as rx]
            [kdtree :as kd]
            [uxbox.util.webworkers :as ww]))

(enable-console-print!)

(defonce state (volatile! nil))

(let [points (for [x (range 0 4000 10)
                   y (range 0 4000 10)]
               #js [x y])
      tree (kd/create2d (into-array points))]
  (vreset! state tree))

(println "initialized")

(defmulti handler :cmd)

;; (defmethod handler :init
;;   [message]
;;   (time
;;    (let [points (for [x (range 0 4000 10)
;;                       y (range 0 4000 10)]
;;                   #js [x y])
;;          tree (kd/create2d (into-array points))]
;;      (vreset! state tree))))

(defmethod handler :query
  [{:keys [id x y] :as message}]
  (let [results (.nearest @state #js [x y] 2)
        results (js->clj results)]
    (ww/send! {:results results
               :id id})))

(defmethod handler :default
  [message]
  (println "Unexpected message:" message))

;; --- Main Entry point

(defonce stream
  (-> (ww/input-stream)
      (rx/on-value handler)))


;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.ui.workspace.align
  "Shape alignmen impl."
  (:require [beicon.core :as rx]
            [lentes.core :as l]
            [uxbox.state :as st]
            [uxbox.shapes :as sh]
            [uxbox.ui.workspace.base :as wb]
            [uxbox.util.geom.point :as gpt]
            [uxbox.util.webworkers :as ww]))

;; (defonce
;; (defn- will-mount
;;   [own]
;;   (let [[

(defonce w (ww/init "/js/worker-align.js"))
;; ;; (ww/send! w {:cmd :init})
;; (-> (ww/ask! w {:cmd :query :x 11 :y 11})
;;     (rx/on-value #(println "response:" %)))

;; (defn test
;;   []
;;   (js/console.time "test")
;;   (-> (ww/ask! w {:cmd :query :x 11 :y 11})
;;       (rx/on-value (fn [message]
;;                      (js/console.timeEnd "test")))))

(defn translate
  [{:keys [x1 y1] :as shape}]
  (println "query" [x1 y1])
  (->> (ww/ask! w {:cmd :query :x x1 :y y1})
       (rx/map (fn [{:keys [results]}]
                 (println "response" results)
                 (if (seq results)
                   (let [dx (- (:x2 shape) (:x1 shape))
                         dy (- (:y2 shape) (:y1 shape))
                         p1 (apply gpt/point (ffirst results))
                         p2 (gpt/add p1 [dx dy])]
                     (assoc shape
                            :x1 (:x p1)
                            :y1 (:y p1)
                            :x2 (:x p2)
                            :y2 (:y p2)))
                   shape)))))

;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.ui.workspace.movement
  "Shape movement in workspace logic."
  (:require [beicon.core :as rx]
            [uxbox.rstore :as rs]
            [uxbox.state :as st]
            [uxbox.shapes :as sh]
            [uxbox.ui.core :as uuc]
            [uxbox.ui.workspace.base :as wb]
            [uxbox.ui.workspace.align :as align]
            [uxbox.data.shapes :as uds]
            [uxbox.util.geom.point :as gpt]))

(declare initialize)
(declare handle-movement)


(defn- coords-delta
  [[old new]]
  (gpt/subtract new old))

(defonce mouse-delta-s
  (->> wb/mouse-viewport-s
       (rx/sample 10)
       (rx/buffer 2 1)
       (rx/map coords-delta)
       (rx/share)))


;; --- Public Api

(defn watch-move-actions
  []
  (as-> uuc/actions-s $
    (rx/filter #(= "ui.shape.move" (:type %)) $)
    (rx/on-value $ initialize)))

;; --- Implementation

(defn- initialize
  [{shapes :payload}]
  (let [stoper (->> uuc/actions-s
                    (rx/map :type)
                    (rx/filter empty?)
                    (rx/take 1))]
    (as-> wb/mouse-delta-s $
      (rx/take-until stoper $)
      (rx/scan (fn [acc delta]
                 (mapv #(sh/move % delta) acc)) shapes $)
      (rx/mapcat (fn [items]
                   (->> (apply rx/of items)
                        (rx/mapcat align/translate)
                        (rx/reduce conj []))) $)
      (rx/subscribe $ handle-movement nil #(println "complete")))))

(defn- handle-movement
  [delta]
  (doseq [shape delta]
    (rs/emit! (uds/update-exact-position shape))))


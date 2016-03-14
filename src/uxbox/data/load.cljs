;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.data.load
  (:require [hodgepodge.core :refer [local-storage]]
            [beicon.core :as rx]
            [lentes.core :as l]
            [uxbox.rstore :as rs]
            [uxbox.router :as r]
            [uxbox.state :as st]
            [uxbox.schema :as sc]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:const ^:private +persistent-keys+
  [:auth])

(defn- persist-state!
  [state]
  (assoc! local-storage ::auth (:auth state)))

(defn init
  []
  (let [lens (l/select-keys +persistent-keys+)
        stream (->> (l/focus-atom lens st/state)
                    (rx/from-atom)
                    (rx/dedupe)
                    (rx/debounce 1000)
                    (rx/tap #(println "[save]")))]
    (rx/on-value stream #(persist-state! %))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-data
  "Load data from local storage."
  []
  (reify
    rs/UpdateEvent
    (-apply-update [_ state]
      (let [auth (::auth local-storage)]
        (assoc state :auth auth)))))


;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.repo
  "A main interface for access to remote resources."
  (:refer-clojure :exclude [do])
  (:require [uxbox.repo.core :as urc]
            [uxbox.repo.auth]
            [beicon.core :as rx]))

(defn do
  "Perform a side effectfull action accesing
  remote resources."
  ([type]
   (rx/from-promise
    (urc/-do type nil)))
  ([type data]
   (rx/from-promise
    (urc/-do type data))))

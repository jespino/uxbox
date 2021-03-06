;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.repo
  "A main interface for access to remote resources."
  (:refer-clojure :exclude [do])
  (:require [uxbox.repo.core :refer (request)]
            [uxbox.repo.auth]
            [uxbox.repo.users]
            [uxbox.repo.projects]
            [uxbox.repo.pages]
            [beicon.core :as rx]))

(defn req
  "Perform a side effectfull action accesing
  remote resources."
  ([type]
   (request type nil))
  ([type data]
   (request type data)))

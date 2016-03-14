;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.core
  (:require-macros [uxbox.util.syntax :refer [define-once]])
  (:require [uxbox.state :as st]
            [uxbox.router :as rt]
            [uxbox.rstore :as rs]
            [uxbox.ui :as ui]
            [uxbox.data.load :as dl]))

(enable-console-print!)

(define-once :setup
  (println "bootstrap")
  (st/init)
  (rt/init)
  (ui/init)

  (rs/emit! (dl/load-data))

  ;; During development, you can comment the
  ;; following call for disable temprary the
  ;; local persistence.
  (dl/init))

;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.util.webworkers
  "A lightweight layer on top of webworkers api."
  (:require [beicon.core :as rx]
            [uxbox.util.transit :as t]))

;; --- Implementation

(defprotocol IWorker
  (-ask [_ msg] "Send and receive message as rx stream.")
  (-send [_ msg] "Send message and forget."))

(deftype WebWorker [stream wrk]
  IWorker
  (-ask [this message]
    (let [id (random-uuid)
          data (assoc message :id id)
          data (t/encode data)]
      (.postMessage wrk data)
      (->> stream
           (rx/filter #(= (:id %) id))
           (rx/take 1))))

  (-send [this message]
    (let [data (t/encode message)]
      (.postMessage wrk data))))

;; --- Public Api

(defn init
  "Return a initialized webworker instance."
  [path]
  (let [wrk (js/Worker. path)
        bus (rx/bus)]
    (.addEventListener wrk "message"
                       (fn [event]
                         (let [data (.-data event)
                               data (t/decode data)]
                           (rx/push! bus data))))
    (.addEventListener wrk "error"
                       (fn [event]
                         (rx/error! bus event)))

    (WebWorker. (rx/map identity bus) wrk)))

(defn ask!
  [wrk message]
  (-ask wrk message))

(defn send!
  ([message]
   (js/postMessage (t/encode message)))
  ([wrk message]
   (-send wrk message)))

;; --- WebWorker Api (inside)

(defn input-stream
  []
  (rx/create
   (fn [sink]
     (js/addEventListener "message"
                          (fn [event]
                            (sink (t/decode (.-data event))))))))

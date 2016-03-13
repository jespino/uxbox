;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.repo
  "A main interface for access to remote resources."
  (:refer-clojure :exclude [do])
  (:require [httpurr.client.xhr :as http]
            [httpurr.status :as http.status]
            [cognitect.transit :as t]
            [promesa.core :as p]
            [beicon.core :as rx]))

(def ^:private ^:const +uri+
  "http://127.0.0.1:5050/api")

(defn- decode
  [data]
  (let [r (t/reader :json {:handlers {"u" ->UUID}})]
    (t/read r data)))

(defn- encode
  [data]
  (let [w (t/writer :json)]
    (t/write w data)))

(defn- conditional-decode
  [{:keys [body headers] :as response}]
  (if (= (get headers "content-type") "application/transit+json")
    (assoc response :body (decode body))
    response))

(defn- handle-http-status
  [{:keys [status] :as response}]
  (if (http.status/success? status)
    (p/resolved response)
    (p/rejected response)))

(def ^:private ^:const +headers+
  {"content-type" "application/transit+json"})

(defn- send!
  [{:keys [body headers] :as request}]
  (let [headers (merge +headers+ headers)
        request (merge (assoc request :headers headers)
                       (when body {:body (encode body)}))]
    (-> (http/send! request)
        (p/catch (fn [err]
                   (println "[error]:" err)
                   (throw err)))
        (p/then conditional-decode)
        (p/then handle-http-status))))

(defmulti -do
  (fn [type data] type))

(defn do
  "Perform a side effectfull action accesing
  remote resources."
  ([type]
   (-do type nil))
  ([type data]
   (-do type data)))

(defmethod -do :login
  [type data]
  (let [request {:url (str +uri+ "/auth/token")
                 :method :post
                 :body data}]
    (rx/from-promise
     (p/then (send! request)
             (fn [response]
               (println response)
               {:fullname "Cirilla Fiona"
                :photo "/images/favicon.png"
                :username "cirilla"
                :email "cirilla@uxbox.io"})))))

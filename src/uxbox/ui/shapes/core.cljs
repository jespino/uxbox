(ns uxbox.ui.shapes.core
  (:require [cats.labs.lens :as l]
            [cuerdas.core :as str]
            [rum.core :as rum]
            [beicon.core :as rx]
            [uxbox.state :as st]
            [uxbox.shapes :as sh]
            [uxbox.ui.mixins :as mx]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Implementation Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti -shape-render
  (fn [own shape] (:type shape))
  :hierarchy #'sh/+hierarchy+)

(defmulti -render
  sh/dispatch-by-type
  :hierarchy #'sh/+hierarchy+)

(defmulti -render-svg
  sh/dispatch-by-type
  :hierarchy #'sh/+hierarchy+)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Lenses
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:static selected-shapes-l
  (as-> (l/in [:workspace :selected]) $
    (l/focus-atom $ st/state)))

(defn- focus-shape
  [id]
  (as-> (l/in [:shapes-by-id id]) $
    (l/focus-atom $ st/state)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Component Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- shape-render
  [own id]
  (let [shape-l (focus-shape id)
        shape (rum/react shape-l)]
    (-shape-render own shape)))

(def ^:const shape
  (mx/component
   {:render shape-render
    :name "shape"
    :mixins [(mx/local) mx/static rum/reactive]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Actions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce actions-b (rx/bus))

(defn emit-action!
  [type]
  (rx/push! actions-b type))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Attribute transformations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:static ^:private +style-attrs+
  #{:fill :opacity :stroke :stroke-opacity :stroke-width :stroke-type :rx :ry})

(defn- transform-stroke-type
  [attrs]
  (if-let [type (:stroke-type attrs)]
    (let [value (case type
                  :dotted "1,1"
                  :dashed "10,10")]
      (-> attrs
          (assoc! :stroke-dasharray value)
          (dissoc! :stroke-type)))
    attrs))

(defn- extract-style-attrs
  "Extract predefinet attrs from shapes."
  [shape]
  (let [attrs (select-keys shape +style-attrs+)]
    (-> (transient attrs)
        (transform-stroke-type)
        (persistent!))))

(defn- make-debug-attrs
  [shape]
  (let [attrs (select-keys shape [:rotation :width :height :x :y])
        xf (map (fn [[x v]]
                    [(keyword (str "data-" (name x))) v]))]
      (into {} xf attrs)))


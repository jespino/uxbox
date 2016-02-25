(ns uxbox.ui.shapes.icon
  (:require [sablono.core :refer-macros [html]]
            [cuerdas.core :as str]
            [rum.core :as rum]
            [cats.labs.lens :as l]

            [uxbox.rstore :as rs]
            [uxbox.state :as st]
            [uxbox.shapes :as sh]
            [uxbox.data.workspace :as dw]
            [uxbox.ui.keyboard :as kbd]
            [uxbox.ui.shapes.core :as usc]
            [uxbox.util.dom :as dom]))

(defn- focus-shape
  [id]
  (as-> (l/in [:shapes-by-id id]) $
    (l/focus-atom $ st/state)))

(defmethod usc/-render :builtin/icon
  [{:keys [data id] :as shape} _]
  (let [key (str id)
        rfm (sh/-transformation shape)
        attrs (merge {:id key :key key :transform (str rfm)}
                     (usc/extract-style-attrs shape)
                     (usc/make-debug-attrs shape))]
    (html
     [:g attrs data])))

(def ^:private selection-circle-style
  {:fillOpacity "0.5"
   :strokeWidth "1px"
   :vectorEffect "non-scaling-stroke"})

(def ^:private default-selection-props
  {:r 5 :style selection-circle-style
   :fill "#333"
   :stroke "#333"})

(defmethod usc/-shape-render :builtin/icon
  [own item]
  (let [{:keys [id x y width height group]} item
        selected (rum/react usc/selected-shapes-l)
        selected? (contains? selected id)
        {:keys [x y width height]} (sh/-outer-rect item)
        local (:rum/local own)]
    (letfn [(on-mouse-down [event]
              (when-not (:blocked item)
                (cond
                  (and group (:locked (sh/resolve-parent item)))
                  nil

                  (and (not selected?) (empty? selected))
                  (do
                    (dom/stop-propagation event)
                    (usc/emit-action! :shape/movement)
                    (rs/emit! (dw/select-shape id)))

                  (and (not selected?) (not (empty? selected)))
                  (do
                    (dom/stop-propagation event)
                    (if (kbd/shift? event)
                      (rs/emit! (dw/select-shape id))
                      (rs/emit! (dw/deselect-all)
                                (dw/select-shape id))))

                  :else
                  (do
                    (dom/stop-propagation event)
                    ;; (swap! local assoc :init-coords [x y])
                    (usc/emit-action! :shape/movement)))))

            (on-mouse-up [event]
              (cond
                (and group (:locked (sh/resolve-parent item)))
                nil

                :else
                (do
                  (dom/stop-propagation event)
                  (usc/emit-action! :nothing)
                  )))]
      (html
       [:g.shape {:class (when selected? "selected")
                  :on-mouse-down on-mouse-down
                  :on-mouse-up on-mouse-up}
        (usc/-render item #(usc/shape %))
        (when selected?
          [:g.controls
           [:rect {:x x :y y :width width :height height :stroke-dasharray "5,5"
                   :style {:stroke "#333" :fill "transparent"
                           :stroke-opacity "1"}}]
           [:circle.top-left (merge default-selection-props
                                    {:cx x :cy y})]
           [:circle.top-right (merge default-selection-props
                                     {:cx (+ x width) :cy y})]
           [:circle.bottom-left (merge default-selection-props
                                       {:cx x :cy (+ y height)})]
           [:circle.bottom-right (merge default-selection-props
                                        {:cx (+ x width) :cy (+ y height)})]])]))))



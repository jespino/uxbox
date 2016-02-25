(ns uxbox.ui.workspace.canvas
  (:require [sablono.core :as html :refer-macros [html]]
            [rum.core :as rum]
            [beicon.core :as rx]
            [cats.labs.lens :as l]
            [goog.events :as events]
            [uxbox.router :as r]
            [uxbox.rstore :as rs]
            [uxbox.state :as st]
            [uxbox.xforms :as xf]
            [uxbox.shapes :as sh]
            [uxbox.util.lens :as ul]
            [uxbox.library.icons :as _icons]
            [uxbox.data.projects :as dp]
            [uxbox.data.workspace :as dw]
            [uxbox.ui.mixins :as mx]
            [uxbox.util.geom.point :as gpt]
            [uxbox.util.dom :as dom]
            [uxbox.util.data :refer (parse-int)]
            [uxbox.ui.keyboard :as kbd]
            [uxbox.ui.shapes :as us]
            [uxbox.ui.workspace.base :as wb]
            [uxbox.ui.workspace.canvas.movement]
            [uxbox.ui.workspace.canvas.draw :refer (draw-area)]
            [uxbox.ui.workspace.canvas.ruler :refer (ruler)]
            [uxbox.ui.workspace.canvas.selection :refer (shapes-selection)]
            [uxbox.ui.workspace.canvas.selrect :refer (selrect)]
            [uxbox.ui.workspace.grid :refer (grid)])
  (:import goog.events.EventType))

;; (defn- focus-shape
;;   [id]
;;   (as-> (l/in [:shapes-by-id id]) $
;;     (l/focus-atom $ st/state)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Background
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn background-render
  []
  (html
   [:rect
    {:x 0 :y 0 :width "100%" :height "100%" :fill "white"}]))

(def background
  (mx/component
   {:render background-render
    :name "background"
    :mixins [mx/static]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Shape
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: Temporal approach, pending big refactor.

;; (declare shape)

;; (defn shape-render
;;   [own id selected]
;;   (let [{:keys [id x y group] :as item} (rum/react (focus-shape id))
;;         selected? (contains? selected id)
;;         local (:rum/local own)]
;;     (letfn [(on-mouse-down [event]
;;               (when-not (:blocked item)
;;                 (cond
;;                   (and group (:locked (sh/resolve-parent item)))
;;                   nil

;;                   (and (not selected?) (empty? selected))
;;                   (do
;;                     (dom/stop-propagation event)
;;                     (swap! local assoc :init-coords [x y])
;;                     (wb/emit-interaction! :shape/movement)
;;                     (rs/emit! (dw/select-shape id)))

;;                   (and (not selected?) (not (empty? selected)))
;;                   (do
;;                     (dom/stop-propagation event)
;;                     (swap! local assoc :init-coords [x y])
;;                     (if (kbd/shift? event)
;;                       (rs/emit! (dw/select-shape id))
;;                       (rs/emit! (dw/deselect-all)
;;                                 (dw/select-shape id))))

;;                   :else
;;                   (do
;;                     (dom/stop-propagation event)
;;                     (swap! local assoc :init-coords [x y])
;;                     (wb/emit-interaction! :shape/movement)))))

;;             (on-mouse-up [event]
;;               (cond
;;                 (and group (:locked (sh/resolve-parent item)))
;;                 nil

;;                 :else
;;                 (do
;;                   (dom/stop-propagation event)
;;                   (wb/emit-interaction! :nothing)
;;                   )))]
;;       (html
;;        [:g.shape {:class (when selected? "selected")
;;                   :on-mouse-down on-mouse-down
;;                   :on-mouse-up on-mouse-up}
;;         (sh/-render item #(shape % selected))]))))

;; (def ^:static shape
;;   (mx/component
;;    {:render shape-render
;;     :name "shape"
;;     :mixins [(mx/local {}) rum/reactive mx/static]}))

;; (def ^:private selection-circle-style
;;   {:fillOpacity "0.5"
;;    :strokeWidth "1px"
;;    :vectorEffect "non-scaling-stroke"})

;; (def ^:private default-selection-props
;;   {:r 5 :style selection-circle-style
;;    :fill "#333"
;;    :stroke "#333"})

;; (defn shape-render
;;   [own id]
;;   (let [item (rum/react (focus-shape id))
;;         {:keys [x y width height group]} item
;;         selected (rum/react wb/selected-shapes-l)
;;         selected? (contains? selected id)
;;         {:keys [x y width height]} (sh/-outer-rect item)
;;         local (:rum/local own)]
;;     (letfn [(on-mouse-down [event]
;;               (when-not (:blocked item)
;;                 (cond
;;                   (and group (:locked (sh/resolve-parent item)))
;;                   nil

;;                   (and (not selected?) (empty? selected))
;;                   (do
;;                     (dom/stop-propagation event)
;;                     (wb/emit-interaction! :shape/movement)
;;                     (rs/emit! (dw/select-shape id)))

;;                   (and (not selected?) (not (empty? selected)))
;;                   (do
;;                     (dom/stop-propagation event)
;;                     (if (kbd/shift? event)
;;                       (rs/emit! (dw/select-shape id))
;;                       (rs/emit! (dw/deselect-all)
;;                                 (dw/select-shape id))))

;;                   :else
;;                   (do
;;                     (dom/stop-propagation event)
;;                     ;; (swap! local assoc :init-coords [x y])
;;                     (wb/emit-interaction! :shape/movement)))))

;;             (on-mouse-up [event]
;;               (cond
;;                 (and group (:locked (sh/resolve-parent item)))
;;                 nil

;;                 :else
;;                 (do
;;                   (dom/stop-propagation event)
;;                   (wb/emit-interaction! :nothing)
;;                   )))]
;;       (html
;;        [:g.shape {:class (when selected? "selected")
;;                   :on-mouse-down on-mouse-down
;;                   :on-mouse-up on-mouse-up}
;;         (sh/-render item #(shape %))
;;         (when selected?
;;           [:g.controls
;;            [:rect {:x x :y y :width width :height height :stroke-dasharray "5,5"
;;                    :style {:stroke "#333" :fill "transparent"
;;                            :stroke-opacity "1"}}]
;;            [:circle.top-left (merge default-selection-props
;;                                     {:cx x :cy y})]
;;            [:circle.top-right (merge default-selection-props
;;                                      {:cx (+ x width) :cy y})]
;;            [:circle.bottom-left (merge default-selection-props
;;                                        {:cx x :cy (+ y height)})]
;;            [:circle.bottom-right (merge default-selection-props
;;                                         {:cx (+ x width) :cy (+ y height)})]])]))))

;; (def ^:static shape
;;   (mx/component
;;    {:render shape-render
;;     :name "shape"
;;     :mixins [(mx/local {}) rum/reactive mx/static]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Canvas
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- canvas-render
  [own {:keys [width height id] :as page}]
  (println "canvas-render")
  (let [workspace (rum/react wb/workspace-l)]
    (html
     [:svg.page-canvas {:x wb/canvas-start-x
                        :y wb/canvas-start-y
                        :ref (str "canvas" id)
                        :width width
                        :height height}
      (background)
      (grid 1)
      [:svg.page-layout {}
       #_(shapes-selection shapes-selected)
       [:g.main {}
        (for [item (:shapes page)]
          (-> (us/shape item)
              (rum/with-key (str item))))
        (draw-area)]]])))

(def canvas
  (mx/component
   {:render canvas-render
    :name "canvas"
    :mixins [mx/static rum/reactive]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Viewport Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn viewport-render
  [own]
  (let [workspace (rum/react wb/workspace-l)
        page (rum/react wb/page-l)
        drawing? (:drawing workspace)
        zoom 1]
    (letfn [(on-mouse-down [event]
              (dom/stop-propagation event)
              (when-not (empty? (:selected workspace))
                (rs/emit! (dw/deselect-all)))
              (if-let [shape (:drawing workspace)]
                (wb/emit-interaction! :draw/shape)
                (wb/emit-interaction! :draw/selrect)))
            (on-mouse-up [event]
              (dom/stop-propagation event)
              (wb/emit-interaction! :nothing))]
      (html
       [:svg.viewport {:width wb/viewport-width
                       :height wb/viewport-height
                       :ref "viewport"
                       :class (when drawing? "drawing")
                       :on-mouse-down on-mouse-down
                       :on-mouse-up on-mouse-up}
        [:g.zoom {:transform (str "scale(" zoom ", " zoom ")")}
         (if page
           (canvas page))
         (ruler)
         (selrect)]]))))

(defn- viewport-did-mount
  [own]
  (letfn [(translate-point-to-viewport [pt]
            (let [viewport (mx/get-ref-dom own "viewport")
                  brect (.getBoundingClientRect viewport)
                  brect (gpt/point (parse-int (.-left brect))
                                   (parse-int (.-top brect)))]
              (gpt/subtract pt brect)))

          (translate-point-to-canvas [pt]
            (let [viewport (mx/get-ref-dom own "viewport")]
              (when-let [canvas (dom/get-element-by-class "page-canvas" viewport)]
                (let [brect (.getBoundingClientRect canvas)
                      bbox (.getBBox canvas)
                      brect (gpt/point (parse-int (.-left brect))
                                       (parse-int (.-top brect)))
                      bbox (gpt/point (.-x bbox) (.-y bbox))]
                  (-> (gpt/add pt bbox)
                      (gpt/subtract brect))))))

          (on-key-down [event]
            (when (kbd/space? event)
              (wb/emit-interaction! :scroll/viewport)))

          (on-key-up [event]
            (when (kbd/space? event)
              (wb/emit-interaction! :nothing)))

          (on-mousemove [event]
            (let [wpt (gpt/point (.-clientX event)
                                 (.-clientY event))
                  vppt (translate-point-to-viewport wpt)
                  cvpt (translate-point-to-canvas wpt)
                  event {:ctrl (kbd/ctrl? event)
                         :shift (kbd/shift? event)
                         :window-coords wpt
                         :viewport-coords vppt
                         :canvas-coords cvpt}]
              (rx/push! wb/mouse-b event)))]

    (let [key1 (events/listen js/document EventType.MOUSEMOVE on-mousemove)
          key2 (events/listen js/document EventType.KEYDOWN on-key-down)
          key3 (events/listen js/document EventType.KEYUP on-key-up)]
      (assoc own ::key1 key1 ::key2 key2 ::key3 key3))))

(defn- viewport-will-unmount
  [own]
  (let [key1 (::key1 own)
        key2 (::key2 own)
        key3 (::key3 own)]
    (events/unlistenByKey key1)
    (events/unlistenByKey key2)
    (events/unlistenByKey key3)
    (dissoc own ::key1 ::key2 ::key3)))

(defn- viewport-transfer-state
  [old-own own]
  (let [data (select-keys old-own [::key1 ::key2 ::key3])]
    (merge own data)))

(def viewport
  (mx/component
   {:render viewport-render
    :name "viewport"
    :did-mount viewport-did-mount
    :will-unmount viewport-will-unmount
    :transfer-state viewport-transfer-state
    :mixins [rum/reactive]}))

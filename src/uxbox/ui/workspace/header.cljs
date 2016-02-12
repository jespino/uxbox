(ns uxbox.ui.workspace.header
  (:require [sablono.core :as html :refer-macros [html]]
            [rum.core :as rum]
            [beicon.core :as rx]
            [uxbox.router :as r]
            [uxbox.rstore :as rs]
            [uxbox.data.workspace :as dw]
            [uxbox.ui.workspace.base :as wb]
            [uxbox.ui.icons :as i]
            [uxbox.ui.users :as ui.u]
            [uxbox.ui.navigation :as nav]
            [uxbox.ui.mixins :as mx]))

(defn on-download-clicked
  [event page]
  (let [content (.-innerHTML (.getElementById js/document "page-layout"))
        width (:width page)
        height (:height page)
        html (str "<svg width='" width  "' height='" height  "'>" content "</svg>")
        data (js/Blob. #js [html] #js {:type "application/octet-stream"})
        url (.createObjectURL (.-URL js/window) data)]
    (set! (.-href (.-currentTarget event)) url)))

(defn header-render
  [own]
  (let [page (rum/react wb/page-l)
        flags (rum/react wb/flags-l)
        toggle #(rs/emit! (dw/toggle-tool %))]
    (html
     [:header#workspace-bar.workspace-bar
      [:div.main-icon
       (nav/link (r/route-for :dashboard/projects) i/logo-icon)]
      [:div.project-tree-btn
       {:on-click (partial toggle :workspace/pagesmngr)}
       i/project-tree
       [:span (:name page)]]
      [:div.workspace-options
       [:ul.options-btn
        [:li.tooltip.tooltip-bottom {:alt "Shapes (Ctrl + Shift + S)"}
         i/shapes]
        [:li.tooltip.tooltip-bottom {:alt "Elements (Ctrl + Shift + E)"}
         i/puzzle]
        [:li.tooltip.tooltip-bottom {:alt "Icons (Ctrl + Shift + I)"}
         i/icon-set]
        [:li.tooltip.tooltip-bottom {:alt "Layers (Ctrl + Shift + L)"}
         i/layers]]
       [:ul.options-btn
        [:li.tooltip.tooltip-bottom {:alt "Undo (Ctrl + Z)"}
         i/undo]
        [:li.tooltip.tooltip-bottom {:alt "Redo (Ctrl + Shift + Z)"}
         i/redo]]
       [:ul.options-btn
        ;; TODO: refactor
        [:li.tooltip.tooltip-bottom
         {:alt "Export (Ctrl + E)"}
         ;; page-title
         [:a {:download (str (:name page) ".svg")
              :href "#" :on-click on-download-clicked}
          i/export]]
        [:li.tooltip.tooltip-bottom
         {:alt "Image (Ctrl + I)"}
         i/image]]
       [:ul.options-btn
        [:li.tooltip.tooltip-bottom
         {:alt "Ruler (Ctrl + R)"
          :class (when (contains? flags :workspace/ruler) "selected")
          :on-click (partial toggle :workspace/ruler)}
         i/ruler]
        [:li.tooltip.tooltip-bottom
         {:alt "Grid (Ctrl + G)"
          :class (when (contains? flags :workspace/grid) "selected")
          :on-click (partial toggle :workspace/grid)}
         i/grid]
        [:li.tooltip.tooltip-bottom
         {:alt "Align (Ctrl + A)"}
         i/alignment]
        [:li.tooltip.tooltip-bottom
         {:alt "Organize (Ctrl + O)"}
         i/organize]]
       [:ul.options-btn
        [:li.tooltip.tooltip-bottom
         {:alt "Multi-canvas (Ctrl + M)"}
         i/multicanvas]]]
      (ui.u/user)])))

(def header
  (mx/component
   {:render header-render
    :name "workspace-header"
    :mixins [rum/reactive]}))

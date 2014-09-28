(ns testreagent.se3
  (:require-macros [testreagent.se2 :refer [go-loop-sub]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async]
            [om.core :as om  :include-macros true]
            ;[ajax.core :refer (GET)]
            [sablono.core :as html :refer-macros [html]]))


;; event-bus
(def event-bus (async/chan))
(def event-bus-pub (async/pub event-bus first))


;; model
(def game-model (atom nil))


;; UI
(defn guess-number-game [game-model owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chan (async/chan)})

    om/IRenderState
    (render-state [_ _]
      (html
       [:div
        "Rate eine Zahl zwischen 1 und 100 "
        [:p]
        "Dein Tipp:"
        [:input {:type "number"
                 :value (:my-guess game-model)
                 :disabled (:game-over? game-model)
                 ;;:on-change (fn [e] (async/put! chan [:update-guess (-> e .-target .-value int)]))
                 
                 }]
        [:button {:type "button"
                  :disabled (:game-over? game-model)
                  ;;:on-click (fn [e] (async/put! chan [:make-guess]))
                  }
         "Rate!"]
        [:button {:type "button"
                  :disabled (not (:game-over? game-model))
                  ;;:on-click (fn [e] (async/put! chan [:reset-game]))
                  }
         "Start"]
        [:div (:message game-model)]]
       ))))




(defn run []
  (async/put! event-bus [:reset-game])

  (om/root guess-number-game game-model
           {:target (.getElementById js/document "app2")}))

(run)

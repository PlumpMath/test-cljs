(ns testreagent.se3
  (:require-macros [testreagent.se2 :refer [go-loop-sub]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async]
            [om.core :as om  :include-macros true]
            ;[ajax.core :refer (GET)]
            [sablono.core :as html :refer-macros [html]]
            [testreagent.game :as g]
            [goog.events :as events])
  (:import [goog.events EventType]))

(enable-console-print!)

;; event-bus
(def event-bus (async/chan))
(def event-bus-pub (async/pub event-bus first))

;; model
(def game-model (atom nil))

;; UI
(defn guess-number-game [game-model owner ]
  (reify
    om/IRender
    (render [_]
      (let [event-bus (om/get-shared owner :event-bus)]
        (html
         [:div
          "Guess a number between 1 and 100"
          [:p]
          "Your guess:" 
          [:input {:type "number"
                   :value (:my-guess game-model)
                   :disabled (:game-over? game-model)
                   :on-key-down (fn [e]
                                 (if (= 13 (.-keyCode e))
                                   (async/put! event-bus [:make-guess])))
                   :on-change (fn [e] (async/put! event-bus [:update-guess (-> e .-target .-value int)]))}]
          [:button {:type "button"
                    :disabled (:game-over? game-model)
                    :on-click (fn [e] (async/put! event-bus [:make-guess]))}
           "Guess!"]
          [:button {:type "button"
                    :disabled (not (:game-over? game-model))
                    :on-click (fn [e] (async/put! event-bus [:reset-game]))}
           "Start"]
          [:div (:message game-model)]]
         )))))




;; when game is started
(go-loop-sub event-bus-pub :reset-game [_]
             (reset! game-model (g/game-init)))

;; when button 'Rate' is clicked
(go-loop-sub event-bus-pub :make-guess [_] (swap! game-model g/make-guess))

;; when guessed number gets edited
(go-loop-sub event-bus-pub :update-guess [_ number]
             (swap! game-model (partial g/edit-my-guess number)))

;; Start the app
(defn run []
  (async/put! event-bus [:reset-game])

  (om/root guess-number-game game-model
           {:target (.getElementById js/document "app2")
            :shared {:event-bus event-bus }}))

(run)

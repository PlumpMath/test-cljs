 (ns testreagent.se2
  (:require-macros [testreagent.se2 :refer [go-loop-sub]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async]
            [testreagent.game :as g]))

(enable-console-print!)

;; event-bus
(def event-bus (async/chan))
(def event-bus-pub (async/pub event-bus first))

;; model
(def game-model (atom nil))

;; UI
(defn guess-number-game [chan]
  [:div
   "Rate eine Zahl zwischen 1 und 100 "
   [:p]
   "Dein Tipp:"
   [:input {:type "number"
            :value (:my-guess @game-model)
            :disabled (:game-over? @game-model)
            :on-key-down (fn [e] (if (= 13 (.-keyCode e))
                                   (async/put! chan [:make-guess])))
            :on-change (fn [e] (async/put! chan [:update-guess (-> e .-target .-value int)]))}]
   [:button {:type "button"
             :disabled (:game-over? @game-model)
             :on-click (fn [e] (async/put! chan [:make-guess]))}
    "Rate!"]
   [:button {:type "button"
             :disabled (not (:game-over? @game-model))
             :on-click (fn [e] (async/put! chan [:reset-game]))}
    "Start"]
   [:div (:message @game-model)]])




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
  (reagent/render-component
   [guess-number-game event-bus]

  (.getElementById js/document "app1")))

(run)

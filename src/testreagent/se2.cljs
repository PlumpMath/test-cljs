(ns testreagent.se2
  (:require-macros [testreagent.se2 :refer [go-loop-sub]]
                   [cljs.core.async.macros :refer [go go-loop]])
    (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async]))

(enable-console-print!)

;; Guess number game

(def event-bus (async/chan))
(def event-bus-pub (async/pub event-bus first))

(def game-model (atom nil))


(defn game-init []
  {:secret-number (->> (.random js/Math) (* 100)(int) (inc))
   :number-of-guesses 0
   :my-guess 50   
   :game-over? false})

(defn game-over? [model]
  (or (= (:secret-number model) (:my-guess model)) 
      (>= (:number-of-guesses model) 7)))

(defn game-message [model]
  (if (:game-over? model)
    (cond
     (= (:secret-number model) (:my-guess model)) "Gewonnen!!"
     (>= (:number-of-guesses model) 7) (str "Verloren. Die gesuchte Zahl war " (:secret-number model) ". Neuer Versuch?"))
    (cond
     (= 0 (:number-of-guesses model)) "Du hast 7 Versuche"
     (> (:secret-number model) (:my-guess model)) (str (:my-guess model) " ist zu klein! Noch " (- 7 (:number-of-guesses model)) " Versuche" )
     (< (:secret-number model) (:my-guess model)) (str (:my-guess model) " ist zu groß! Noch " (- 7 (:number-of-guesses model)) " Versuche" ))))

;; UI helper funcrtions

(defn- make-guess [chan]
  (async/put! chan [:make-guess]))

(defn- update-my-guess [chan guess]
  (async/put! chan [:update-guess guess]))

(defn- reset-game [chan]
  (async/put! chan [:reset-game]))

;; UI
(defn guess-number-game [chan]
  [:div
   "Rate eine Zahl zwischen 1 und 100 "
   [:p]
   "Dein Tipp:"
   [:input {:type "number"
            :value (:my-guess @game-model)
            :disabled (:game-over? @game-model)
            :on-change (fn [e] (update-my-guess chan (-> e .-target .-value int)))}]
   [:button {:type "button"
             :disabled (:game-over? @game-model)
             :on-click (fn [e] (make-guess chan))}
    "Rate!"]
   [:button {:type "button"
             :disabled (not (:game-over? @game-model))
             :on-click (fn [e] (reset-game chan))}
    "Start"]
   [:div (:message @game-model)]])



;; when game is started
(go-loop-sub event-bus-pub :reset-game [_]
             (reset! game-model (game-init)))

;; when button 'Rate' is clicked
(go-loop-sub event-bus-pub :make-guess [_]
             (swap! game-model
                    (fn [old]
                      (as-> old x
                            (update-in x [:number-of-guesses] inc)
                            (assoc-in x [:game-over?] (game-over? x))
                            (assoc-in x [:message] (game-message x))))))

;; when guessed number is changed
(go-loop-sub event-bus-pub :update-guess [_ number]
             (swap! game-model
                    (fn [old]
                      (assoc-in old [:my-guess] number))))

(defn run []
  (reset-game event-bus)
  (reagent/render-component
   [guess-number-game event-bus]
   (.-body js/document)))

(run)




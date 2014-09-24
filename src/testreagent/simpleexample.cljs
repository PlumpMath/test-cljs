(ns testreagent.simpleexample
  (:require [reagent.core :as reagent :refer [atom]]))

(def timer (atom (js/Date.)))
(def time-color (atom "#f34"))
(def mailtext (atom ""))

(def my-tree (atom {:a "a"
                    :b "b"
                    :c {:c1 "c1" :c2 "c2" :c3 "c3"}
                    }))

(defn update-time [time]
  ;; Update the time every 1/10 second to be accurate...
  (js/setTimeout #(reset! time (js/Date.)) 100))

(defn greeting [message]
  [:h1 message])

(defn clock []
  (update-time timer)
  (if (<= 0 (.indexOf @mailtext "jetzt"))
    (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
      [:div.example-clock
       {:style {:color @time-color}}
       time-str])
    [:div.example-clock
       {:style {:color @time-color}} "Keine Uhr"]
    )
  )

(defn color-input []
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-change #(reset! time-color (-> % .-target .-value))}]])

(defn text-area []
  [:div.color-input
   "Tippen Sie \"jetzt\": "
   [:textarea {
            :value @mailtext
            :on-change #(reset! mailtext (-> % .-target .-value))}]
   [:div (str (count @mailtext))]])

;; Guess number game



(def game-model (atom nil))

(defn game-init []
  {:secret-number (->> (.random js/Math) (* 100)(int) (inc))
   :number-of-guesses 0
   :my-guess 50
   :game-over? false})

(defn game-over? [model]
  (or 
   (= (:secret-number model) (:my-guess model)) 
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

(defn guess-number-game []
  [:div
   "Rate eine Zahl zwischen 1 und 100 "
   [:p]
   "Dein Tipp:"
   [:input {:type "number"
            :value (:my-guess @game-model)
            :disabled (:game-over? @game-model)
            :on-change (fn [e]
                         (swap! game-model
                                (fn [old]
                                  (assoc-in old
                                            [:my-guess]
                                            (-> e .-target .-value int))
                                  )))}]
   [:button {:type "button"
             :disabled (:game-over? @game-model)
             :on-click (fn [e]
                         (swap! game-model
                                (fn [old]
                                  (as-> old x
                                        (update-in x [:number-of-guesses] inc)
                                        (assoc-in x [:game-over?] (game-over? x))
                                        (assoc-in x [:message] (game-message x))))))}
    "Rate!"]
   [:button
    {:type "button"
     :disabled (not (:game-over? @game-model))
     :on-click (fn [e]
                 (reset! game-model (game-init)))}
    "Start"]
;;   [:div (str @game-model)]
   [:div (:message @game-model)]
   ])


(defn simple-example []
  [:div
   ;;[clock]
   [guess-number-game]
   ;;[color-input]
   ;;[text-area]
   ])


(defn run []
  (reset! game-model (game-init))
  (reagent/render-component
   [simple-example]
   (.-body js/document)))

;;(run)


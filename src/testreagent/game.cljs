(ns testreagent.game)

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
     (= (:secret-number model) (:my-guess model))
     (str "Gewonnen in " (:number-of-guesses model) " von 7 Versuchen. Die gesuchte Zahl ist " (:secret-number model))
     (>= (:number-of-guesses model) 7) (str "Verloren. Die gesuchte Zahl war " (:secret-number model) ". Neuer Versuch?"))
    (cond
     (= 0 (:number-of-guesses model)) "Du hast 7 Versuche"
     (> (:secret-number model) (:my-guess model)) (str (:my-guess model) " ist zu klein! Noch " (- 7 (:number-of-guesses model)) " Versuche" )
     (< (:secret-number model) (:my-guess model)) (str (:my-guess model) " ist zu gross! Noch " (- 7 (:number-of-guesses model)) " Versuche" ))))

(defn make-guess [old-model]
  (as-> old-model x
        (update-in x [:number-of-guesses] inc)
        (assoc-in x [:game-over?] (game-over? x))
        (assoc-in x [:message] (game-message x))))

(defn edit-my-guess [number old-model]
  (assoc-in old-model [:my-guess] number))

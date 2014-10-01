(ns testreagent.game)

(defn game-init
  ""
  []
  {:secret-number (->> (.random js/Math) (* 100)(int) (inc))
   :number-of-guesses 0
   :my-guess 50
   :my-guesses #{}
   :game-over? false})

(defn game-over?
  "Returns true if the model is in state 'game-over'"
  [model]
  (or (contains? (:my-guesses model) (:secret-number model)) 
      (>= (count (:my-guesses model)) 7)))

(defn game-message
  "Returns a text message to be shown to the user"
  [model]
  (let [number-of-guesses (count (:my-guesses model))]
    (if (:game-over? model)
      (cond
       (= (:secret-number model) (:my-guess model))
       (str "You won in " number-of-guesses " of 7 trials. The secret number is " (:secret-number model))
       (>= number-of-guesses 7) (str "Game over. The secret number was " (:secret-number model) ". Play again?"))
      (cond
       (= 0 number-of-guesses) "You have got 7 trials."
       (> (:secret-number model) (:my-guess model)) (str (:my-guess model) " is to small! "
                                                         (- 7 number-of-guesses)
                                                         " guesses left. Your attempts so far: " (sort  (:my-guesses model)))
       (< (:secret-number model) (:my-guess model)) (str (:my-guess model) " is to big! Still " (- 7 number-of-guesses) " attempts left. "
                                                         "Your attempts so far: " (sort (:my-guesses model)))))))

(defn make-guess
  "Returns a new model incorporating the value to the key :my-guess of the old-model in the set under the key :my-guesses.
Values to the keys :game-over? and :message are updated too"
  [old-model]
  (as-> old-model x
        (update-in x [:my-guesses] (fn [my-guesses] (set (cons (:my-guess x) my-guesses) )))
        (assoc-in x [:game-over?] (game-over? x))
        (assoc-in x [:message] (game-message x))))

(defn edit-my-guess
  "Returns a new model with value to key :my-guess updated to 'number'"
  [number old-model]
  (assoc-in old-model [:my-guess] number))

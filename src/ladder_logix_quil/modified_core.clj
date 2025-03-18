(ns ladder-logix-quil.modified-core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:gen-class))

;; Data structure definitions using maps instead of defrecord

;; Map creation helper functions
(defn create-contact [x y normally-closed? name state branch-id]
  {:type :contact
   :x x
   :y y
   :normally-closed? normally-closed?
   :name name
   :state state
   :branch-id branch-id})

(defn create-coil [x y name state]
  {:type :coil
   :x x
   :y y
   :name name
   :state state})

(defn create-connection [x1 y1 x2 y2 branch-id]
  {:type :connection
   :x1 x1
   :y1 y1
   :x2 x2
   :y2 y2
   :branch-id branch-id})

(defn create-node [x y connected-branches]
  {:type :node
   :x x
   :y y
   :connected-branches connected-branches})

;; Sample ladder diagram with parallel branches
(def initial-state
  {:contacts [(create-contact 150 100 false "X1" false 0)
              (create-contact 300 100 true "X2" false 0)
              ;; Parallel branch 1
              (create-contact 200 160 false "X3" false 1)
              ;; Parallel branch 2
              (create-contact 200 220 true "X4" false 2)
              ;; Parallel branch 3
              (create-contact 200 280 false "X5" false 3)
              (create-contact 350 280 true "X6" false 3)
              ;; Second rung
              (create-contact 150 350 false "X7" false 4)
              (create-contact 300 350 false "Y1" false 4)] ; Feedback from first coil

   :coils [(create-coil 500 190 "Y1" false)
           (create-coil 500 350 "Y2" false)]

   :connections [;; Main horizontal lines
                 (create-connection 50 100 150 100 0)
                 (create-connection 200 100 300 100 0)
                 (create-connection 350 100 400 100 0)

                 ;; Parallel branch 1
                 (create-connection 100 100 100 160 0)
                 (create-connection 100 160 200 160 1)
                 (create-connection 250 160 400 160 1)

                 ;; Parallel branch 2
                 (create-connection 100 160 100 220 0)
                 (create-connection 100 220 200 220 2)
                 (create-connection 250 220 400 220 2)

                 ;; Parallel branch 3
                 (create-connection 100 220 100 280 0)
                 (create-connection 100 280 200 280 3)
                 (create-connection 250 280 350 280 3)
                 (create-connection 400 280 400 220 3)

                 ;; Vertical connection from parallel branches to coil
                 (create-connection 400 100 400 280 0)
                 (create-connection 400 190 500 190 0)
                 (create-connection 550 190 600 190 0)

                 ;; Second rung
                 (create-connection 50 350 150 350 4)
                 (create-connection 200 350 300 350 4)
                 (create-connection 350 350 500 350 4)
                 (create-connection 550 350 600 350 4)

                 ;; Power rails
                 (create-connection 50 100 50 350 0)
                 (create-connection 600 190 600 350 0)]

   :nodes [(create-node 100 160 [0 1])
           (create-node 100 220 [0 2])
           (create-node 100 280 [0 3])
           (create-node 400 100 [0])
           (create-node 400 160 [1])
           (create-node 400 220 [2])
           (create-node 400 280 [3])]

   :input-states {"X1" false
                  "X2" false
                  "X3" false
                  "X4" false
                  "X5" false
                  "X6" false
                  "X7" false}})

;; Logic evaluation functions

(defn eval-contact [contact inputs]
  (let [input-state (get inputs (:name contact) false)]
    (if (:normally-closed? contact)
      (not input-state)
      input-state)))

(defn collect-branch-contacts [contacts branch-id]
  (filter #(= (:branch-id %) branch-id) contacts))

(defn eval-branch [contacts branch-id inputs]
  (let [branch-contacts (collect-branch-contacts contacts branch-id)]
    (if (empty? branch-contacts)
      true  ; Empty branch is considered true (passthrough)
      (reduce (fn [result contact]
                (and result (eval-contact contact inputs)))
              true
              branch-contacts))))

(defn find-branches-for-coil [connections nodes coil]
  (let [coil-x (:x coil)
        coil-y (:y coil)
        ;; Find connections directly connected to the coil
        coil-connections (filter #(and (= (:x2 %) coil-x)
                                       (= (:y2 %) coil-y))
                                 connections)
        ;; Get branch IDs from these connections
        direct-branch-ids (map :branch-id coil-connections)]
    ;; Find all branches that are connected through nodes
    (loop [current-branches direct-branch-ids
           all-branches #{}]
      (if (empty? current-branches)
        all-branches
        (let [branch-id (first current-branches)
              connected-branches (reduce (fn [acc node]
                                           (if (some #(= % branch-id) (:connected-branches node))
                                             (concat acc (:connected-branches node))
                                             acc))
                                         []
                                         nodes)]
          (recur (concat (rest current-branches)
                         (filter #(not (contains? all-branches %)) connected-branches))
                 (conj all-branches branch-id)))))))

(defn evaluate-ladder [state]
  (let [inputs (:input-states state)
        contacts (:contacts state)
        coils (:coils state)
        connections (:connections state)
        nodes (:nodes state)
        ;; Create updated coils
        updated-coils (map (fn [coil]
                             (let [relevant-branches (find-branches-for-coil connections nodes coil)
                                   ;; Evaluate each branch (parallel branches use OR logic)
                                   branch-results (map #(eval-branch contacts % inputs) relevant-branches)
                                   ;; If any branch is true, the result is true (OR logic)
                                   rung-result (some true? branch-results)]
                               (assoc coil :state rung-result)))
                           coils)
        ;; Update input states with coil states (for feedback)
        updated-inputs (reduce (fn [inputs coil]
                                 (assoc inputs (:name coil) (:state coil)))
                               inputs
                               updated-coils)]
    (assoc state
           :coils updated-coils
           :input-states updated-inputs)))

;; Drawing functions

(defn draw-contact [contact]
  (q/stroke 0)
  (q/stroke-weight 2)
  (q/fill 255)
  (q/rect-mode :center)

  ;; Draw the contact
  (q/rect (:x contact) (:y contact) 50 30)

  ;; Draw the normally closed line if needed
  (when (:normally-closed? contact)
    (q/line (- (:x contact) 15) (- (:y contact) 15)
            (+ (:x contact) 15) (+ (:y contact) 15)))

  ;; Label the contact
  (q/fill 0)
  (q/text-align :center :center)
  (q/text (:name contact) (:x contact) (:y contact))

  ;; Color based on state
  (q/no-fill)
  (let [active? (eval-contact contact (:input-states q/state))]
    (if active?
      (q/stroke 0 255 0)  ;; Green for active
      (q/stroke 255 0 0)) ;; Red for inactive
    (q/rect (:x contact) (:y contact) 54 34)))

(defn draw-coil [coil]
  (q/stroke 0)
  (q/stroke-weight 2)
  (q/fill 255)
  (q/ellipse-mode :center)

  ;; Draw the coil
  (q/ellipse (:x coil) (:y coil) 50 30)

  ;; Label the coil
  (q/fill 0)
  (q/text-align :center :center)
  (q/text (:name coil) (:x coil) (:y coil))

  ;; Color based on state
  (q/no-fill)
  (if (:state coil)
    (q/stroke 0 255 0)  ;; Green for active
    (q/stroke 255 0 0)) ;; Red for inactive
  (q/ellipse (:x coil) (:y coil) 54 34))

(defn draw-connection [connection state]
  (let [branches-state (reduce (fn [acc [branch-id active?]]
                                 (assoc acc branch-id active?))
                               {}
                               (map (fn [branch-id]
                                      [branch-id (eval-branch (:contacts state) branch-id (:input-states state))])
                                    (range 0 10))) ; Support up to 10 branches
        active? (get branches-state (:branch-id connection) false)]

    ;; Choose color based on branch state
    (if active?
      (q/stroke 0 255 0)  ;; Green for active
      (q/stroke 0))       ;; Black for inactive

    (q/stroke-weight 2)
    (q/line (:x1 connection) (:y1 connection)
            (:x2 connection) (:y2 connection))))

(defn draw-node [node]
  (q/fill 0)
  (q/ellipse (:x node) (:y node) 8 8))

(defn draw-state [state]
  (q/background 240)

  ;; Draw all connections
  (doseq [connection (:connections state)]
    (draw-connection connection state))

  ;; Draw all nodes
  (doseq [node (:nodes state)]
    (draw-node node))

  ;; Draw all contacts
  (doseq [contact (:contacts state)]
    (draw-contact contact))

  ;; Draw all coils
  (doseq [coil (:coils state)]
    (draw-coil coil))

  ;; Draw instruction panel
  (q/fill 0)
  (q/text-align :left :top)
  (q/text "Click on contacts to toggle input states" 50 20)
  (q/text "Press 'r' to reset all states" 50 40)
  (q/text "Ladder includes parallel branches (OR logic)" 50 60))

(defn toggle-contact-at [state x y]
  (let [contacts (:contacts state)
        inputs (:input-states state)
        ;; Find if we clicked on a contact
        clicked-contact (first (filter (fn [contact]
                                         (and (<= (- (:x contact) 25) x (+ (:x contact) 25))
                                              (<= (- (:y contact) 15) y (+ (:y contact) 15))))
                                       contacts))]
    (if clicked-contact
      ;; Toggle the input state for this contact
      (assoc state :input-states
             (update inputs (:name clicked-contact) not))
      state)))

(defn mouse-clicked [state event]
  (toggle-contact-at state (:x event) (:y event)))

(defn key-pressed [state event]
  (case (:key event)
    :r (assoc state :input-states
              (zipmap (keys (:input-states state))
                      (repeat false)))
    state))

(defn update-state [state]
  (evaluate-ladder state))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  initial-state)

(defn -main []
  (q/sketch
   :title "Ladder Logic Interpreter with Parallel Branches"
   :size [700 450]
   :setup setup
   :update update-state
   :draw draw-state
   :mouse-clicked mouse-clicked
   :key-pressed key-pressed
   :features [:keep-on-top]
   :middleware [m/fun-mode]))

;; Run the application
(-main)
# Ladder Logic Interpreter with Quil

This project is a visual ladder logic interpreter implemented in Clojure using the Quil library (a Clojure wrapper for Processing). It allows you to simulate and visualize ladder logic diagrams commonly used in industrial control systems.

## Table of Contents

- [Overview](#overview)
- [Core Namespace Explanation](#core-namespace-explanation)
  - [Data Structures](#data-structures)
  - [Ladder Diagram Evaluation](#ladder-diagram-evaluation)
  - [Drawing Functions](#drawing-functions)
  - [User Interaction](#user-interaction)
  - [Application Setup](#application-setup)
- [Usage](#usage)
- [Example](#example)
- [Features](#features)

## Overview

Ladder logic is a programming language used to develop software for programmable logic controllers (PLCs) used in industrial control applications. The language features contacts, coils, and connections arranged in rungs resembling a ladder, hence the name.

This application provides a visual interface to create, simulate, and interact with ladder logic diagrams. It supports both series and parallel branches, allowing for complex logic implementation.

## Core Namespace Explanation

The `ladder-logix-quil.core` namespace is the main component of this application, containing all the logic for rendering and simulating ladder diagrams.

### Data Structures

The core data structures in the application are:

1. **Contact**: Represents an input contact in the ladder diagram
   ```clojure
   {:type :contact
    :x x                     ; X-coordinate position
    :y y                     ; Y-coordinate position
    :normally-closed? nc     ; Whether the contact is normally closed (NC) or normally open (NO)
    :name name               ; Identifier for the contact (e.g., "X1")
    :state state             ; Current state (true or false)
    :branch-id branch-id}    ; ID of the branch this contact belongs to
   ```

2. **Coil**: Represents an output device in the ladder diagram
   ```clojure
   {:type :coil
    :x x                     ; X-coordinate position
    :y y                     ; Y-coordinate position
    :name name               ; Identifier for the coil (e.g., "Y1")
    :state state}            ; Current state (true or false)
   ```

3. **Connection**: Represents a wire that connects components
   ```clojure
   {:type :connection
    :x1 x1                   ; Starting X-coordinate
    :y1 y1                   ; Starting Y-coordinate
    :x2 x2                   ; Ending X-coordinate
    :y2 y2                   ; Ending Y-coordinate
    :branch-id branch-id}    ; ID of the branch this connection belongs to
   ```

4. **Node**: Represents a junction where multiple branches connect
   ```clojure
   {:type :node
    :x x                          ; X-coordinate position
    :y y                          ; Y-coordinate position
    :connected-branches branches} ; IDs of branches connected at this node
   ```

The application state is a map containing vectors of these structures and the input states:

```clojure
{:contacts [...]       ; Vector of contacts
 :coils [...]          ; Vector of coils
 :connections [...]    ; Vector of connections
 :nodes [...]          ; Vector of nodes
 :input-states {...}}  ; Map of input names to boolean states
```

### Ladder Diagram Evaluation

The ladder logic evaluation is performed by these key functions:

1. **`eval-contact`**: Evaluates a single contact's state based on input values
   ```clojure
   (defn eval-contact [contact inputs]
     (let [input-state (get inputs (:name contact) false)]
       (if (:normally-closed? contact)
         (not input-state)
         input-state)))
   ```

2. **`eval-branch`**: Evaluates an entire branch (series of contacts) using AND logic
   ```clojure
   (defn eval-branch [contacts branch-id inputs]
     (let [branch-contacts (collect-branch-contacts contacts branch-id)]
       (if (empty? branch-contacts)
         true  ; Empty branch is considered true (passthrough)
         (reduce (fn [result contact]
                   (and result (eval-contact contact inputs)))
                 true
                 branch-contacts))))
   ```

3. **`find-branches-for-coil`**: Determines which branches affect a specific coil
   ```clojure
   (defn find-branches-for-coil [connections nodes coil]
     ; ... implementation ...
     )
   ```

4. **`evaluate-ladder`**: The main function that evaluates the entire ladder diagram, updating coil states
   ```clojure
   (defn evaluate-ladder [state]
     ; ... implementation ...
     )
   ```

The logic follows these principles:
- Contacts in series use AND logic
- Parallel branches use OR logic
- Normally closed contacts invert their input
- Coil outputs can be fed back as inputs (e.g., for latching circuits)

### Drawing Functions

The application uses Quil to render the ladder diagram:

1. **`draw-contact`**, **`draw-coil`**, **`draw-connection`**, **`draw-node`**: Draw individual components
2. **`draw-state`**: Main drawing function that renders the complete ladder diagram

Components are color-coded based on their state:
- Green: Active/Powered
- Red: Inactive contacts or coils
- Black: Inactive connections

### User Interaction

User interaction is handled by:

1. **`toggle-contact-at`**: Toggles the state of a contact when clicked
2. **`mouse-clicked`**: Event handler for mouse clicks
3. **`key-pressed`**: Event handler for key presses (e.g., 'r' to reset states)

### Application Setup

The application setup and initialization:

1. **`setup`**: Initializes the Quil sketch with the initial ladder diagram
2. **`update-state`**: Updates the application state on each frame
3. **`-main`**: Creates and runs the Quil sketch

## Usage

To run the application:

1. Ensure you have Clojure and Leiningen installed
2. Clone the repository
3. Run `lein run` in the project directory

## Example

The application includes a sample ladder diagram with:

- Two rungs (one with parallel branches)
- Multiple contacts (both NO and NC)
- Two coils
- Feedback from the first coil to the second rung

Interact with the diagram by:
- Clicking on contacts to toggle their input state
- Pressing 'r' to reset all states
- Observing how the changes propagate through the ladder

## Features

- Interactive ladder logic simulation
- Support for both series and parallel logic
- Normally open (NO) and normally closed (NC) contacts
- Visual feedback with color-coded states
- Branch-based organization for complex diagrams
- Coil feedback support for latching and other complex circuits

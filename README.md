# ladder_logix_quil

I'll explain the enhanced ladder logic interpreter that supports parallel branches:

### Core Data Structures

1. **Contact Record**
   - Represents input contacts (switches) in ladder logic
   - Contains position (x, y), a normally-closed flag, name, state, and branch-id
   - Branch-id identifies which logical circuit branch the contact belongs to

2. **Coil Record**
   - Represents output devices (like relays, motors, etc.)
   - Contains position, name, and state

3. **Connection Record**
   - Represents wires connecting components
   - Contains start/end coordinates and branch-id

4. **Node Record**
   - New in version 2
   - Represents junction points where branches connect
   - Contains position and a list of connected branch IDs
   - Essential for evaluating parallel (OR) logic

### Logic Evaluation System

The evaluation follows these steps:

1. **Contact Evaluation** (`eval-contact`):
   - Checks if a contact is closed or open based on its input state
   - Handles normally-closed vs. normally-open contacts

2. **Branch Evaluation** (`eval-branch`):
   - Collects all contacts in a specific branch
   - Uses AND logic to evaluate them (all contacts in series must be true)

3. **Branch Finding** (`find-branches-for-coil`):
   - Identifies all branches that can power a specific coil
   - Uses the node connections to follow the electrical path

4. **Ladder Evaluation** (`evaluate-ladder`):
   - For each coil, finds all connected branches
   - Evaluates each branch independently 
   - Uses OR logic between branches (if any branch is true, the coil activates)
   - Updates coil states and prepares for feedback (coils affecting contacts)

### Visualization System

1. **Drawing Components**:
   - `draw-contact`: Draws contacts as rectangles with labels
   - `draw-coil`: Draws coils as ellipses with labels
   - `draw-connection`: Draws wires with colors indicating active/inactive state
   - `draw-node`: Draws junction points as small circles

2. **Active State Visualization**:
   - Active components show in green
   - Inactive components show in red (contacts/coils) or black (wires)
   - This provides visual feedback on the current flow through the system

### Interaction System

1. **Mouse Interaction** (`mouse-clicked`, `toggle-contact-at`):
   - Detects clicks on contacts
   - Toggles the input state of the clicked contact

2. **Keyboard Interaction** (`key-pressed`):
   - 'r' key resets all input states to false

### Sample Ladder Configuration

The example shows a complex ladder with:

1. A main horizontal rung (branch 0) with X1 and X2 in series
2. Three parallel branches:
   - Branch 1 with a single X3 contact
   - Branch 2 with a single X4 contact
   - Branch 3 with X5 and X6 in series
3. A second rung showing feedback where Y1's state affects X7

This structure demonstrates both AND logic (within branches) and OR logic (between parallel branches).

### Why This Matters

In real PLC programming, both series (AND) and parallel (OR) logic are essential:
- Series connections (contacts in the same branch) require ALL conditions to be true (AND)
- Parallel branches require ANY path to be true to activate the coil (OR)

This version properly simulates actual industrial ladder logic behavior, where complex combinations of conditions control outputs.

Would you like me to explain any specific part in more detail?


=================================================================================
brooksg44: @workspace /explain 

Workspace: The provided Clojure code defines a ladder logic interpreter using the Quil library for graphical representation. Ladder logic is a programming language used to develop software for programmable logic controllers (PLCs) commonly used in industrial automation. The code is structured into several sections, including data structure definitions, logic evaluation functions, drawing functions, and event handling.

The data structure definitions include records for `Contact`, `Coil`, `Connection`, and `Node`. These records represent the components of a ladder diagram, such as contacts (switches), coils (outputs), connections (wires), and nodes (junction points for parallel branches). The `initial-state` map defines a sample ladder diagram with parallel branches, specifying the positions and states of contacts, coils, connections, and nodes, as well as the initial input states.

The logic evaluation functions are responsible for determining the state of the ladder diagram based on the input states. The `eval-contact` function evaluates the state of a contact based on its normally closed or open configuration. The `collect-branch-contacts` function gathers all contacts belonging to a specific branch. The `eval-branch` function evaluates the state of a branch by checking if all its contacts are in the correct state. The `find-branches-for-coil` function identifies all branches connected to a specific coil, including those connected through nodes. The `evaluate-ladder` function updates the state of the ladder diagram by evaluating all coils and updating the input states based on the coil states.

The drawing functions use Quil to render the ladder diagram on the screen. The `draw-contact`, `draw-coil`, `draw-connection`, and `draw-node` functions handle the graphical representation of contacts, coils, connections, and nodes, respectively. The `draw-state` function draws the entire ladder diagram, including an instruction panel for user interaction.

Event handling functions manage user interactions with the ladder diagram. The `toggle-contact-at` function toggles the state of a contact when it is clicked. The `mouse-clicked` function handles mouse click events by calling `toggle-contact-at`. The `key-pressed` function handles key press events, allowing the user to reset all input states by pressing the 'r' key. The `update-state` function updates the ladder diagram state by calling `evaluate-ladder`.

The `setup` function initializes the Quil sketch with a frame rate, color mode, and the initial state. The `-main` function defines and runs the Quil sketch, setting up the title, size, setup function, update function, draw function, mouse click handler, key press handler, and middleware. The application is executed by calling `(-main)`, which starts the ladder logic interpreter with parallel branches.

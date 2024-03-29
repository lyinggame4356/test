# system.check

Extend test.check to test stateful systems.

## Usage

Create a simulator with an initial state and call target object.

After the bindings (which allow destructuring), each pair is a condition
followed by a piece of state that may be generated if that condition is met.

Unlike a cond statement, all matching conditions are eligible for generation.
Once the eligible commands are decided, one will be randomly chosen to be
generated.

```clojure
(defspec simulate-something 1000
  (system.check/simulator
    {:initial-state  (fn [])
     :initial-target (fn [])
     :next-state     (fn [state command result])
     :error?         (fn [state command result])
     :run-command?   (fn [state command])
     :on-error       system.check/on-error}
    [state target]
    (can-foo? state)
      [:apply `my.domain/foo gen/int]
    (can-bar? state)
      [:-> `my.domain/bar (gen/one-of [(gen/return nil) gen/pos-int])]
    (can-baz? state)
      [:->> 'my.domain/baz (gen/elements (vec (get-things state)))]))
```


## Details on Simulation, Execution, and Shrinking

The state machine runs the simulation in 2 phases. 

1) The first time to generate command lists suitable for the application state
   as it evolves
2) The second time applying the generated commands to execute them.

The state machine is used during BOTH phases, first to enable generation of
commands that affect previous results, then to validate actual results against
simulated state. It is important to keep this in mind when writing your state
machine.

The two phase approach is necessary to allow shrinking over command lists when
an error is encountered. Because the full command-list is represented with the
same data structure as produced by a test.check generator, shrunk variations can
be created using it.

When generating commands, we call next-state on state machine without
executing any command. In place of that command's result, we use a Var
instance which the state must treat as a black box representing the result.
The state machine may store those vars to use them for future command
generation or verification.  So we might have (MyState. #{(Var. 1) (Var. 2)}
{:x (Var. 1)}) after some commands have been generated. (ie. the state contains
unresolved variables)

Each Var is numbered and permanantly associated to its command. Even when
some commands are removed during shrinking, the numbers associated with
a command will not change.

Once a test is completely generated, it is passed to the runner which
executes each command, this time passing the actual result of running the
command to the state machine's `next-state` as well as `postcondition` or
`error?` functions. Any vars in commands must refer to previously executed
statements, and so before that statement is executed, any vars in the command
are replaced with the corresponding value.

If an exception is thrown or the `error?` or `postcondition` functions
indicate a bad state, the test has failed and now moves into the shrinking
stage.

If a command throws an exception, the error? and postcondition functions can
also accept that as valid. In that case, the keep-result-var? function should
usually return false so that subsequent operations are skipped if they expect
there to be a valid result in that var position.

When shrinking, we first minimize the number of commands needed to reproduce,
then attempt to shrink the arguments those commands are given. To do that we
again use the state machine. Each command subset is validated to ensure that
all vars a command refers to will exist in the subset, then to ensure that
the state preconditions should be expected to pass. This can greatly reduce
the number of invalid tests generated.

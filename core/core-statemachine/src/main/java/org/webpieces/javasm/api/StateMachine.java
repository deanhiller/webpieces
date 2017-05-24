package org.webpieces.javasm.api;

import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;


/**
 */
public interface StateMachine
{

    /**
     * Creates the initial state of the state machine.
     * @param stateMachineId is the id of the StateMachine the state will exist in.
     * @param state the initial state.
     * @return A Memento which contains the state that moves around on the state machine.
     */
    public Memento createMementoFromState(String stateMachineId, State state);

    public State fireEvent(Memento memento, Object event);
    
    /**
     * The fireEvent has to be either synchronized or virtually single threaded.  This method
     * will stack up the calls and ONLY execute them one after the other such that transitions
     * in the statemachine are atomic
     */
    public CompletableFuture<State> fireAsyncEvent(Memento currentState, Object event);

    /**
     * Creates a State with the given name.
     * @param name the name of the state.
     */
    public State createState(String name);

    /**
     * @param startState is the starting state
     * @param endState is the end state
     * @param events are the Event's that trigger the Transition
     * @return the newly created Transition
     */
    public Transition createTransition(State startState, State endState, Object... events);
    public Transition createTransition(State[] startStates, State endState, Object... events);

    /**
     * @param l is the ActionListener for the action that occurs on entry in any State in this
     *  StateMachine
     */
    public StateMachine addGlobalStateEntryAction(ActionListener l);

    /**
     * @param l is the ActionListener for the action that occurs on exit of any State in this
     *  StateMachine
     */
    public StateMachine addGlobalStateExitAction(ActionListener l);

}

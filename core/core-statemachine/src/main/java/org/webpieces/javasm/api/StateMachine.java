package org.webpieces.javasm.api;

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

}

package org.webpieces.javasm.api;

import java.io.Serializable;

/**
 */
public interface Memento extends Serializable
{
    /**
     * @return the name of the State the Memento is currently in.
     */
    public State getCurrentState();
}

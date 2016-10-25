package org.webpieces.httpcommon.api;

import org.webpieces.nio.api.handlers.DataListener;

/**
 * The SwitchableDataListener lets us set a DataListener on a per-protocol basis. This
 * way when we connect to a socket we can give it a particular datalistener (this one)
 * but then if we want to change how data is processed, we just tell this datalistener
 * that the protocol has changed.
 *
 */
public interface SwitchableDataListener extends DataListener {

    /**
     * Adds a protocol -> listener pair.
     *
     * @param protocol
     * @param listener
     */
    void put(Protocol protocol, DataListener listener);


    /**
     * Sets the active protocol.
     *
     * @param protocol
     */
    void setProtocol(Protocol protocol);


    /**
     * Gets the listener for a particular protocol.
     *
     * @param protocol
     * @return
     */
    DataListener getDataListener(Protocol protocol);
}

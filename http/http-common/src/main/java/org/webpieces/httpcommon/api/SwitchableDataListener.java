package org.webpieces.httpcommon.api;

import org.webpieces.nio.api.handlers.AsyncDataListener;
import org.webpieces.nio.api.handlers.DataListener;

public interface SwitchableDataListener extends AsyncDataListener {
    void put(Protocol protocol, DataListener listener);
    void setProtocol(Protocol protocol);
    DataListener getDataListener(Protocol protocol);
}

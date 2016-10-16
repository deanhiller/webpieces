package org.webpieces.httpcommon.api;

import org.webpieces.httpcommon.impl.SwitchableDataListenerImpl;

public class SwitchableDataListenerFactory {
    static public SwitchableDataListener createSwitchableDataListener(HttpSocket socket, CloseListener closeListener) {
        return new SwitchableDataListenerImpl(socket, closeListener);
    }

    static public SwitchableDataListener createSwitchableDataListener(HttpSocket socket) {
        return new SwitchableDataListenerImpl(socket);
    }
}

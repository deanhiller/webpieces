package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public interface AsyncDataListener extends DataListener {

	void connectionOpened(TCPChannel proxy, boolean isReadyForWrites);

}

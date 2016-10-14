package org.webpieces.nio.api.handlers;

import org.webpieces.nio.api.channels.TCPChannel;

public interface AsyncDataListener extends DataListener {

	void connectionOpened(TCPChannel proxy, boolean isReadyForWrites);

}

package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

public interface AsyncDataListener extends DataListener {

	/**
	 * This is called twice on SSL channels, once for connection before handshake so one
	 * could timeout if no value MSG is received.  ie. in the case of http, if no parsed
	 * http message comes through in X time, you can then close the connection.  Timing
	 * out on bytes is quite useless but timing out on a valid message is good stuff.
	 * 
	 * @param proxy
	 * @param isReadyForWrites
	 */
	void connectionOpened(TCPChannel proxy, boolean isReadyForWrites);

}

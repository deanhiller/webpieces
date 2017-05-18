package org.webpieces.frontend2.api;

import org.webpieces.frontend2.impl.ProtocolType;
import org.webpieces.nio.api.channels.ChannelSession;

public interface FrontendSocket {

	/**
	 * If http/2, reason will be sent to client, otherwise in http1.1, the socket is simply closed with no info
	 */
	void close(String reason);

	ChannelSession getSession();
	
	public ProtocolType getProtocol();
	
}

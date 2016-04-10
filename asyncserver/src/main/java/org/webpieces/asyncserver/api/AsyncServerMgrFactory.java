package org.webpieces.asyncserver.api;

import org.webpieces.asyncserver.impl.AsyncServerManagerImpl;
import org.webpieces.nio.api.ChannelManager;

public class AsyncServerMgrFactory {

	public static AsyncServerManager createChannelManager(String id, ChannelManager props) {
		return new AsyncServerManagerImpl();
	}
}

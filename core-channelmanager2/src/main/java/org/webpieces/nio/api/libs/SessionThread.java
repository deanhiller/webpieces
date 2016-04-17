package org.webpieces.nio.api.libs;

public interface SessionThread {
	
	@Deprecated
	public SessionContext getSessionState();
	
	public ChannelSession getSession();
	
}

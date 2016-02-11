package org.playorm.nio.api.libs;

public interface SessionThread {
	
	@Deprecated
	public SessionContext getSessionState();
	
	public ChannelSession getSession();
	
}

package org.playorm.nio.api.libs;

import java.util.Map;

//Use ChannelSession instead
@Deprecated
public interface SessionContext {

	public String getSessionId();
	
	public Map<String, Object> getSessionProperties();
	
	public Map getSessionState();
	
}

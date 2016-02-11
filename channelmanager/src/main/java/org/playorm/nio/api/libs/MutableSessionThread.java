package org.playorm.nio.api.libs;

/** 
 * A platform has access to the MutableSessionThread where clients of the
 * platform only have access to the SessionThread so they can't change the context
 * 
 * @author fastdragon
 */
public interface MutableSessionThread extends SessionThread {

	@Deprecated
	public void setSessionState(SessionContext o);
	
	public void setSession(ChannelSession s);
	
}

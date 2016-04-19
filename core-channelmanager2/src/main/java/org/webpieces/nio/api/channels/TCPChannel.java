package org.webpieces.nio.api.channels;

/**
 * @author Dean Hiller
 */
public interface TCPChannel extends Channel {
	
	public boolean getKeepAlive();
	public void setKeepAlive(boolean b);
	
	public int getSoTimeout();
	
}

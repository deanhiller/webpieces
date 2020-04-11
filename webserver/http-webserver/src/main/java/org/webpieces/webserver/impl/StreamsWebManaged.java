package org.webpieces.webserver.impl;

public interface StreamsWebManaged {

	public String getCategory();
	
	public int getMaxBodySizeToSend();
	
	public void setMaxBodySizeSend(int maxSize);
}

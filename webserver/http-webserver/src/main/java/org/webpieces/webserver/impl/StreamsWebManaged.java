package org.webpieces.webserver.impl;

public interface StreamsWebManaged {

	public String getCategory();
	
	public int getMaxBodySize();
	
	public void setMaxBodySize(int maxSize);
}

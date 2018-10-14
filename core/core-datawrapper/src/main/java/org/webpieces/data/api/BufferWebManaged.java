package org.webpieces.data.api;

public interface BufferWebManaged {

	public String getCategory();
	
	public void setBufferPoolSize(int size);
	public int getBufferPoolSize();
}

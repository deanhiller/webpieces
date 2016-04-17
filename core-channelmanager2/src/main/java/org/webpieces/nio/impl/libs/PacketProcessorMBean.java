package org.webpieces.nio.impl.libs;

public interface PacketProcessorMBean {
	
	public void setSeparator(byte[] bytes);
	public byte[] getSeparator();
}

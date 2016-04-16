package org.webpieces.nio.impl.libs;

import org.webpieces.nio.api.libs.PacketProcessor;
import org.webpieces.nio.api.libs.PacketProcessorFactory;

class DefaultPackProcessorFactory implements PacketProcessorFactory, PacketProcessorMBean {

	private byte[] separator;
	
	public PacketProcessor createPacketProcessor(Object id) {
		return new HeaderTrailerProcessor(id, separator);
	}

	public void setSeparator(byte[] bytes) {
		separator = bytes;
	}

	public byte[] getSeparator() {
		return separator;
	}

}

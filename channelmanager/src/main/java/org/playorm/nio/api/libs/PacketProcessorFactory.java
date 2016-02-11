package org.playorm.nio.api.libs;

public interface PacketProcessorFactory {

	public PacketProcessor createPacketProcessor(Object id);
}

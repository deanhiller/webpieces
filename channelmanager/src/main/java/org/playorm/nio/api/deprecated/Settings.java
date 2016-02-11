package org.playorm.nio.api.deprecated;

import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.libs.SSLEngineFactory;

/**
 * This is used to configure the SSL and Packet layers if you are using them.
 */
public class Settings {

	private SSLEngineFactory sslFactory;
	private PacketProcessorFactory procFactory;

    /**
     * 
     * Creates an instance of Settings.
     * @param sslFactory The sslFactory to be used for encryption
     * @param procFactory The packet processor factory which buffers until a whole packet is received and
     *     wraps puts headers and tails on packets to demarcate a full packet.
     */
	public Settings(SSLEngineFactory sslFactory, PacketProcessorFactory procFactory) {
		this.sslFactory = sslFactory;
		this.procFactory = procFactory;
	}
	
	public SSLEngineFactory getSSLEngineFactory() {
		return sslFactory;
	}
	
	public PacketProcessorFactory getPacketProcessorFactory() {
		return procFactory;
	}
}

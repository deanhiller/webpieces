package org.webpieces.nio.impl.cm.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.libs.PacketListener;
import org.webpieces.nio.api.libs.PacketProcessor;


class PacProxyDataHandler implements DataListener, PacketListener {

	private static final Logger log = Logger.getLogger(PacProxyDataHandler.class.getName());
	private PacTCPChannel channel;
	private DataListener handler;
	private PacketProcessor packetProcessor;

	public PacProxyDataHandler(PacTCPChannel channel, PacketProcessor p, DataListener handler) {
		this.channel = channel;
		this.handler = handler;
		this.packetProcessor = p;
		//this.realChannel = (TCPChannel)channel.getRealChannel();
	}
	
	public void incomingData(Channel realChannel, ByteBuffer chunk) throws IOException {
		try {
			boolean notified = packetProcessor.incomingData(chunk, null);
		} catch(Exception e) {
			log.log(Level.WARNING, "exception", e);
			handler.failure(channel, chunk, e);
		}
	}
	
	public void farEndClosed(Channel realChannel) {
		handler.farEndClosed(channel);
	}

	public void incomingPacket(ByteBuffer b, Object passthrough) throws IOException {
		//MUST create a new packet here as the same DataChunk is sometimes used
		//since one ByteBuffer can contain multiple packets!!!!
		handler.incomingData(channel, b);
	}

	public void failure(Channel realChannel, ByteBuffer data, Exception e) {
		handler.failure(channel, data, e);
	}

}

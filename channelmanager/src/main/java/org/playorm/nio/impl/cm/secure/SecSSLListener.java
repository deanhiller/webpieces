package org.playorm.nio.impl.cm.secure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.NullWriteCallback;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.SSLListener;
import org.playorm.nio.impl.util.DataChunkWithBuffer;
import org.playorm.nio.impl.util.PacketChunk;


class SecSSLListener implements SSLListener {

	private static final Logger log = Logger.getLogger(SecSSLListener.class.getName());
	
	private SecTCPChannel channel;
	private ConnectionListener cb;
	private DataListener client;
	private boolean isConnected = false;
	
	public SecSSLListener(SecTCPChannel impl) {
		this.channel = impl;
	}
	
	public void encryptedLinkEstablished() throws IOException {
		try {
			channel.resetRegisterForReadState();
		} catch (InterruptedException e) {
			throw new RuntimeException(channel+"Exception occured", e);
		}
		cb.connected(channel);
		isConnected = true;
	}
	


	public void packetEncrypted(ByteBuffer toSocket, Object passThrough) throws IOException {
		OperationCallback h;
		if(passThrough == null){
			h = NullWriteCallback.singleton();
		} else {
			SecProxyWriteHandler handler = (SecProxyWriteHandler)passThrough;
			h = handler;
		}

		channel.getRealChannel().oldWrite(toSocket, h);
	}
	
	public void packetUnencrypted(ByteBuffer out, Object passThrough) throws IOException {
		DataChunkWithBuffer c = (DataChunkWithBuffer) passThrough;
		PacketChunk packet = new PacketChunk(out, c);
		client.incomingData(channel, packet);
	}
	
	public void setClientHandler(DataListener client) {
		this.client = client;
	}
	public boolean isClientRegistered() {
		return client != null;
	}

	public void setConnectCallback(ConnectionListener cb) {
		this.cb = cb;
	}

	public void farEndClosed() {
		if(client != null) //if the client did not register for reads, we can't fire to anyone(thought that would be mighty odd)
			client.farEndClosed(channel);
		else if(!isConnected) {
			log.info("The far end connected and did NOT establish security session and then closed his socket.  " +
					"This is normal behavior if a telnet socket connects to your secure socket and exits " +
					"because the socket was never officially 'connected' as we only fire " +
					"connected AFTER the SSL handshake is done.  You may want to check if" +
					" someone is trying to hack your server though");
		} else
			log.warning("When we called ConnectionListener.connected on YOUR ConnectionListener, " +
					"you forot to call registerForReads so we have not callback handler to call " +
					"to tell you this socket is closed from far end");
	}

	public void runTask(Runnable r) {
		r.run();
	}

	public void closed(boolean clientInitiated) {
//		if(fromEncryptedPacket && !closedAlready)
//			client.farEndClosed(channel);
		//can just drop this...we are using close, not initiateClose
		//which is effective immediately.
	}

	public void feedProblemThrough(Channel c, ByteBuffer b, Exception e) throws IOException {
		client.failure(c, b, e);
	}

}

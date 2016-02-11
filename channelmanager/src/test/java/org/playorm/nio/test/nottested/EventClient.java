package org.playorm.nio.test.nottested;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.SSLEngineFactory;
import org.playorm.nio.api.testutil.MockSSLEngineFactory;


public class EventClient implements ConnectionCallback, DataListener {

	private static final Logger log = Logger.getLogger(EventClient.class.getName());
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		EventClient client = new EventClient();
		client.start();
	}
	
	public void start() throws IOException, InterruptedException {
		ChannelService mgr = ChannelServiceFactory.createDefaultChannelMgr("EventServer");
		mgr.start();
		
		SSLEngineFactory sslFactory = new MockSSLEngineFactory();
		Settings h = new Settings(sslFactory, null);		
		TCPChannel channel = mgr.createTCPChannel("SvrChan", h);
		
		InetAddress addr = InetAddress.getByName("192.168.1.102");
		InetSocketAddress sockAddr = new InetSocketAddress(addr, 801);

		log.info("Connecting to server="+sockAddr);
		channel.oldConnect(sockAddr, this);
	}

	public void connected(Channel channel) throws IOException {
		log.info(channel+"Connected now="+channel.getRemoteAddress());
		channel.registerForReads(this);
			
		//now write out the request and wait for events coming back.....
		String hello = "helloThere";
		ByteBuffer b = ByteBuffer.allocate(100);
		HELPER.putString(b, hello);
		HELPER.doneFillingBuffer(b);
		channel.oldWrite(b);			
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		log.log(Level.WARNING, channel+"Exception", e);
	}

	public void incomingData(Channel channel, DataChunk chunk) throws IOException {
		ByteBuffer b = chunk.getData();
		String s = HELPER.readString(b, b.remaining());
		chunk.setProcessed("EventClient");
		log.info(channel+"Received event="+s);
	}

	public void farEndClosed(Channel channel) {
		log.warning(channel+"Should never have closed from far end");
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warning(channel+"Data not received");
	}

}

package org.webpieces.nio.test.nottested;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.deprecated.ChannelService;
import org.webpieces.nio.api.deprecated.ChannelServiceFactory;
import org.webpieces.nio.api.deprecated.Settings;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.libs.SSLEngineFactory;
import org.webpieces.nio.api.testutil.MockSSLEngineFactory;


/**
 * Server to test events going through firewall
 * 
 * @author dean.hiller
 */
public class EventServer implements ConnectionListener, DataListener {

	private static final Logger log = Logger.getLogger(EventServer.class.getName());
	private static final Timer TIMER = new Timer();
	private Map<Channel, TimerTask> channelToTask = new HashMap<Channel, TimerTask>();
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		EventServer svr = new EventServer();
		svr.start();
	}
	
	public void start() throws IOException, InterruptedException {
		log.info("Attempting to start server");
		ChannelService mgr = ChannelServiceFactory.createDefaultChannelMgr("EventServer");
		mgr.start();
		
		SSLEngineFactory sslFactory = new MockSSLEngineFactory();
		Settings h = new Settings(sslFactory, null);		
		TCPServerChannel svrChannel = mgr.createTCPServerChannel("SvrChan", h);
		
		InetAddress addr = InetAddress.getByName("192.168.1.101");
		InetSocketAddress sockAddr = new InetSocketAddress(addr, 801);
		log.info("binding");
		svrChannel.bind(sockAddr);
		log.info("bound to="+svrChannel.getLocalAddress());
		
		svrChannel.registerServerSocketChannel(this);
		
		log.info(svrChannel+"Server started");
	}

	public void connected(Channel channel) throws IOException {
		log.info(channel+"Connected channel remote="+channel.getRemoteAddress());
		channel.registerForReads(this);
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		log.log(Level.WARNING, channel+"Exception", e);
	}

	/**
	 * 
	 */
	public void incomingData(Channel channel, ByteBuffer chunk) throws IOException {
		log.info(channel+"incoming data");
		ByteBuffer buf = ByteBuffer.allocate(chunk.remaining());
		buf.clear();
		buf.put(chunk);
		buf.flip();
		SendBytesTask task = new SendBytesTask(buf, channel);
		channelToTask.put(channel, task);
		TIMER.schedule(task, 0, 3000);
	}

	public void farEndClosed(Channel channel) {
		TimerTask task = channelToTask.get(channel);
		task.cancel();
	}
	
	private static class SendBytesTask extends TimerTask {

		private ByteBuffer b;
		private Channel c;
		public SendBytesTask(ByteBuffer b, Channel c) {
			this.b = b;
			this.c = c;
		}
		@Override
		public void run() {
			try {
				c.oldWrite(b);
				b.rewind();
			} catch(Exception e) {
				log.log(Level.WARNING, c+"TimerTaskException", e);
			}
		}
		
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warning(channel+"Data not received");
	}
}

package org.webpieces.nio.api;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncServerManager;
import org.webpieces.asyncserver.api.AsyncServerMgrFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.futures.Future;

public class TestAsyncServerManager {

	private static final Logger log = LoggerFactory.getLogger(TestAsyncServerManager.class);
	
	/**
	 * Here, we will simulate a bad hacker client that sets his side so_timeout to infinite
	 * and then refuses to read response data back in but keeps writing into our server to
	 * crash the server as it backs up on responses....ie. we keep receiving requests and holding
	 * on to them so memory keeps growing and growing or our write queue keeps growing unbounded
	 * 
	 * so this test ensures we fix that scenario
	 * @throws InterruptedException 
	 */
	//@Test
	public void testSoTimeoutOnSocket() throws InterruptedException {
		BufferCreationPool pool = new BufferCreationPool(false, 2000);
		AsyncServerManager server = AsyncServerMgrFactory.createAsyncServer("server", pool);
		server.createTcpServer("tcpServer", new InetSocketAddress(8080), new ServerListener(pool));
		
		BufferCreationPool pool2 = new BufferCreationPool(false, 2000);
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		Executor executor = Executors.newFixedThreadPool(1);
		ChannelManager mgr = factory.createChannelManager("client", pool2, executor);
		TCPChannel channel = mgr.createTCPChannel("clientChan");
		
		log.info("client keep alive="+channel.getKeepAlive()+" timeout="+channel.getSoTimeout());
		
		Future<Channel, FailureInfo> connect = channel.connect(new InetSocketAddress(8080));
		connect.setResultFunction(p -> runWriting(channel));
		
		Thread.sleep(1000000000);
	}

	private void runWriting(Channel channel) {
		log.info("register for reads");

		DataListener listener = new DataListener() {
			
			@Override
			public void incomingData(Channel channel, ByteBuffer b) {
			}
			
			@Override
			public void farEndClosed(Channel channel) {
				log.info("far end closed");
			}
			
			@Override
			public void failure(Channel channel, ByteBuffer data, Exception e) {
				log.info("failure", e);
			}
		};
		channel.registerForReads(listener );
		
		log.info("starting writing");
		long timeMillis = System.currentTimeMillis();
		int count = 0;
		write(channel, null, timeMillis, count);
	}

	private void write(Channel channel, String reason, long timeMillis, int count) {
		if(count != 0 && count % 100000 == 0) {
			long totalTime = System.currentTimeMillis() - timeMillis;
			long timePer = totalTime / count;
			log.info("time for count="+count+" rounds. time="+totalTime+" perEach2000="+timePer);
		}
		
		int newCount = count+1;
		byte[] data = new byte[2000];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		Future<Channel, FailureInfo> write = channel.write(buffer);
		write.setResultFunction(p -> write(channel, "wrote data from client", timeMillis, newCount))
			.setFailureFunction(p -> finished(channel, "failed from client"))
			.setCancelFunction(p -> finished(channel, "cancelled from client"));
	}

	private void finished(Channel channel, String string) {
		log.info("failed due to reason="+string);
	}

}

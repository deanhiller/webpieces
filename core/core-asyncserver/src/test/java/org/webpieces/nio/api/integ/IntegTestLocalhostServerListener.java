package org.webpieces.nio.api.integ;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

public class IntegTestLocalhostServerListener implements AsyncDataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostServerListener.class);

	public IntegTestLocalhostServerListener() {
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		CompletableFuture<Channel> future = channel.write(b);
		
		future
			.thenAccept(p -> finished("data written", b))
			.whenComplete((r, e) -> fail(channel, b, r, e));
	}

	private void finished(String string, ByteBuffer buffer) {
	}

	private void fail(Channel channel, ByteBuffer b, Void r, Throwable e) {
		if(e != null) {
			log.info("finished exception="+e, e);
			channel.close();
		}
	}
	
	@Override
	public void connectionOpened(TCPChannel proxy, boolean isReadyForWrites) {
		log.info("opened connection="+proxy);
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed="+channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("failure on processing", e);
	}

	@Override
	public void applyBackPressure(Channel channel) {
		//log.info("server apply backpressure");
		channel.unregisterForReads().thenAccept(c -> logIt());
	}

	private void logIt() {
		//log.info("UNregistereD for reads");
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		//log.info("server releasing backpressure");
		channel.registerForReads().thenAccept(c -> logMe());
		
	}

	private void logMe() {
		//log.info("registereD for reads");
	}
}

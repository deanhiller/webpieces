package org.webpieces.nio.api.integ;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;

public class IntegTestClientNotReadListener implements AsyncDataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestClientNotReadListener.class);

	public IntegTestClientNotReadListener() {
	}

	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		log.info("receiving data server..writing now");
		CompletableFuture<Void> future = channel.write(b);

		return future.thenAccept(p -> finished("data written", b));
	}

//	private Void fail(Channel channel, Throwable e) {
//		log.info("finished exception="+e, e);
//		if(!channel.isClosed()) {
//			CompletableFuture<Channel> close = channel.close();
//			close.thenAccept(p -> {
//				log.info("Channel closed");
//			});
//		}
//		return null;
//	}

	private void finished(String string, ByteBuffer buffer) {
		log.info("writing finished reason="+string);
	}

	@Override
	public void connectionOpened(TCPChannel proxy, boolean isReadyForWrites) {
		log.info("connection opened="+proxy+" ready="+isReadyForWrites);
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed="+channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("failure on processing", e);
	}

}

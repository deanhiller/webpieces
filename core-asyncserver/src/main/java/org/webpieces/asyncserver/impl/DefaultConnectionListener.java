package org.webpieces.asyncserver.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;

public class DefaultConnectionListener implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(DefaultConnectionListener.class);
	private DataListener dataListener;
	private ConnectedChannels connectedChannels;
	private ByteBuffer overloadResponse;

	public DefaultConnectionListener(DataListener listener, ConnectedChannels channels) {
		this.dataListener = listener;
		this.connectedChannels = channels;
	}

	@Override
	public void connected(Channel channel) {
		TCPChannel tcpChannel = (TCPChannel) channel;
		
		log.info("keep alive="+tcpChannel.getKeepAlive());
		
		if(overloadResponse != null) {
			//This is annoying.....
			//1. we canNOT do synchronous write as it could block forever (if hacker simulates full nic)
			//2. we can do async but it may never fire data was written so firing close after write is complete may not close forever(leak connection)
			//3. soooo...we must do both async and close on complete with timer to timeout and close on write regardless of success
			//4. lastly, these all could throw exceptions and we don't really care about them at all
			handleOverload(tcpChannel);
			return;
		}
		connectedChannels.addChannel(tcpChannel);
		
		tcpChannel.registerForReads(new ProxyDataListener(connectedChannels, dataListener));
	}

	private void handleOverload(TCPChannel tcpChannel) {
		overloadResponse.mark();
		try {
			tcpChannel.write(overloadResponse);
		} catch(Exception e) {
			//normal behavior in cases where people connect and disconnect before response so
			//we only log it at info...
			log.info("exception trying to send overload response. exc type="+e.getClass());
		}
		
		//close should be moved to two places
		//1. write complete callback should close
		//2. timer in 2 seconds should close if write did not complete in that time
		close(tcpChannel);

		overloadResponse.reset();		
	}

	private void close(TCPChannel tcpChannel) {
		try {
			tcpChannel.close();
		} catch (Exception e) {
			//normal behavior in cases where people connect and disconnect before response so
			//we only log it at info...
			log.info("exception trying to close after sending overload response. exc type="+e.getClass());
		}
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		log.warn("exception from client connecting in. channel="+channel, e);
	}

	public void enableOverloadMode(ByteBuffer overloadResponse) {
		if(overloadResponse.remaining() <= 0)
			throw new IllegalArgumentException("There is 0 remaining bytes in this buffer");
		this.overloadResponse = overloadResponse;
	}

	public void disableOverloadMode() {
		this.overloadResponse = null;
	}

}

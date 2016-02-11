package org.playorm.nio.impl.cm.exception;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;


class ExcProxyDataHandler implements DataListener {

	private static final Logger log = Logger.getLogger(ExcProxyDataHandler.class.getName());
	private ExcTCPChannel channel;
	private DataListener handler;

	public ExcProxyDataHandler(ExcTCPChannel channel, DataListener handler) {
		this.channel = channel;
		this.handler = handler;
	}
	
	public void incomingData(Channel realChannel, DataChunk b) throws IOException {
		try {
			handler.incomingData(channel, b);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception", e);
		}			
	}
	
	public void farEndClosed(Channel realChannel) {
		try {
			handler.farEndClosed(channel);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception", e);
		}			
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		try {
			handler.failure(channel, data, e);
		} catch(Exception ee) {
			log.log(Level.WARNING, channel+"Exception", ee);
		}	
	}

	@Override
	public String toString() {
		return "ExcProxyDataHandler.java[handler="+handler+"]";
	}

	
}

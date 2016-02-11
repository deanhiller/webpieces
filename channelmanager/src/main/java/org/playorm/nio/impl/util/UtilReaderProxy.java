package org.playorm.nio.impl.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;


public class UtilReaderProxy implements DataListener {

    private static final Logger log = Logger.getLogger(UtilReaderProxy.class.getName());
	private Channel channel;
	private DataListener handler;

	public UtilReaderProxy(Channel c, DataListener h) {
		super();
		this.channel = c;
		this.handler = h;
	}

	public void incomingData(Channel realChannel, DataChunk chunk) throws IOException {
		//ByteBuffer b = chunk.getData();
		handler.incomingData(channel, chunk);
        
//        if(b.remaining() > 0) {
//            log.warning("Discarding unread data("+b.remaining()+") from class="+handler.getClass().getName());
//            b.position(b.limit());
//        }
	}

	public void farEndClosed(Channel realChannel) {
		handler.farEndClosed(channel);
	}

	public void failure(Channel realChannel, ByteBuffer data, Exception e) {
		handler.failure(channel, data, e);		
	}

	@Override
	public String toString() {
		return "UtilReaderProxy.java[h="+handler+"]";
	}
	
}

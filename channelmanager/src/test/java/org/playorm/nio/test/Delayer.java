package org.playorm.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.FactoryCreator;


public class Delayer implements DataListener {

	private static final Logger log = Logger.getLogger(Delayer.class.getName());
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	private BufferFactory bufFactory;
	private static Timer timer = new Timer();
	private TCPChannel to;


	public Delayer(TCPChannel to) {
		this.to = to;
		if(bufFactory == null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FactoryCreator.KEY_IS_DIRECT, false);
			FactoryCreator creator = FactoryCreator.createFactory(null);
			bufFactory = creator.createBufferFactory(map);			
		}		
	}
	public void incomingData(Channel channel, DataChunk chunk) throws IOException {
		ByteBuffer b = chunk.getData();
		
		final ByteBuffer newBuffer = bufFactory.createBuffer(channel, b.remaining());
		newBuffer.put(b);
		TimerTask t = new TimerTask() {
			@Override
			public void run() {
				try {
					HELPER.doneFillingBuffer(newBuffer);
					to.oldWrite(newBuffer);
				} catch (Exception e) {
					log.log(Level.WARNING, "exception", e);
				}
			}
			
		};
		timer.schedule(t, 1000);
		
		chunk.setProcessed("Delayer");
	}

	public void farEndClosed(Channel channel) {
		TimerTask t = new TimerTask() {
			@Override
			public void run() {
				try {
					to.oldClose();
				} catch (Exception e) {
					log.log(Level.WARNING, "exception", e);
				}
			}
			
		};
		timer.schedule(t, 1000);
	}
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warning(channel+"Data not received");
	}

	
}

package org.playorm.nio.impl.util;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataChunkImpl implements DataChunkWithBuffer {

	private static final Logger log = Logger.getLogger(DataChunkImpl.class.getName());
	private ByteBuffer data;
	private ProcessedListener listener = null;
	private BufferListener bufferListener;
	private Object id;

	public DataChunkImpl(Object id, ByteBuffer newBuffer, BufferListener l) {
		this.data = newBuffer;
		this.bufferListener = l;
		this.id = id;
	}

	@Override
	public ByteBuffer getData() {
		return data;
	}

	@Override
	public void setProcessed(String namedByteConsumerForLogs) {
		setProcessedImpl();
		releaseBuffer(namedByteConsumerForLogs);
	}

	public void setProcessedImpl() {
		if(listener != null) {
			listener.processed(this);
			listener = null;
		}
	}

	public void setListener(ProcessedListener l) {
		this.listener = l;
	}

	@Override
	public void releaseBuffer(String clientName) {
		if(data != null) {
			if(data.hasRemaining()) {
				log.log(Level.WARNING, id+"Discarding unread data("+data.remaining()+") client that didn't consume data="+clientName, new RuntimeException().fillInStackTrace());
			}
			data.clear();
			bufferListener.releaseBuffer(data);
			data = null;
		}
	}
}

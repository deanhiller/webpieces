package com.webpieces.http2engine.impl.shared.data;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.util.locking.AsyncLock;

public abstract class Stream {

	private Memento currentState;
	private int streamId;
	private long remoteWindowSize;
	private long localWindowSize;
	private long remoteInitialWindowSize;
	private volatile boolean isClosed = false;
	private boolean headersSent;
	protected boolean hasPermit;
	private AsyncLock asyncLock;
	private String logId;

	public Stream(
			String logId,
			int streamId, 
			Memento currentState, 
			long localInitialWindowSize,
			long remoteInitialWindowSize,
			boolean hasPermit
	) {
		this.logId = logId;
		this.streamId = streamId;
		this.asyncLock = new AsyncLock(logId);
		this.currentState = currentState;
		localWindowSize = localInitialWindowSize;
		remoteWindowSize = remoteInitialWindowSize;
		
		this.remoteInitialWindowSize = remoteInitialWindowSize;
		this.hasPermit = hasPermit;
	}

	public int getStreamId() {
		return streamId;
	}

	public Memento getCurrentState() {
		return currentState;
	}

	public long incrementRemoteWindow(long windowSizeIncrement) {
		remoteWindowSize+= windowSizeIncrement;
		if(remoteWindowSize > Integer.MAX_VALUE) {
			throw new StreamException(CancelReasonCode.FLOW_CONTROL_ERROR, logId, streamId, 
					"(remote end bad)remoteWindowSize too large="+remoteWindowSize+" from windows increment="+windowSizeIncrement+" streamId="+streamId);
		}
		return remoteWindowSize;
	}

	public long getRemoteWindowSize() {
		return remoteWindowSize;
	}

	public void updateInitialWindow(long initialWindow) {
		long difference = initialWindow - remoteInitialWindowSize;
		remoteInitialWindowSize = initialWindow; //reset this streams initial window size in case it changes again
		remoteWindowSize += difference;

	}

	public long getLocalWindowSize() {
		return localWindowSize;
	}

	public void incrementLocalWindow(long windowSizeIncrement) {
		localWindowSize += windowSizeIncrement;
		if(localWindowSize > Integer.MAX_VALUE) {
			throw new IllegalStateException("Bug, somehow local window got too large");
		}
	}

	public void setIsClosed(boolean b) {
		isClosed = b;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public boolean isPushStream() {
		return streamId % 2 == 0;
	}

	public boolean isHeadersSent() {
		return headersSent;
	}

	public void setHeadersSent(boolean b) {
		headersSent = b;
	}

	public boolean isHasPermit() {
		return hasPermit;
	}

	public AsyncLock getLock() {
		return asyncLock;
	}
	
}

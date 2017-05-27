package com.webpieces.http2parser.api.dto.error;

import java.util.concurrent.CompletionException;

public abstract class Http2Exception extends CompletionException {

    private static final long serialVersionUID = -2704718008204232741L;
    private int streamId = 0x0;
	private CancelReasonCode reason;

    public Http2Exception(CancelReasonCode reason, String logId, int streamId, String msg) {
        super(logId+":stream"+streamId+":("+reason+") "+msg);
		this.reason = reason;
		this.streamId = streamId;
    }

    public Http2Exception(CancelReasonCode reason, String logId, int streamId, String msg, Throwable e) {
        super(logId+":stream"+streamId+":("+reason+") "+msg, e);
        this.reason = reason;
		this.streamId = streamId;
    }

    public Http2Exception(CancelReasonCode reason, int streamId, String msg) {
        super("stream"+streamId+":("+reason+") "+msg);
		this.reason = reason;
		this.streamId = streamId;
    }

    public Http2Exception(CancelReasonCode reason, int streamId, String msg, Throwable e) {
        super(streamId+":("+reason+") "+msg, e);
        this.reason = reason;
		this.streamId = streamId;
    }
    
    public int getStreamId() {
        return streamId;
    }

    public boolean hasStream() {
        return streamId == 0x0;
    }

	public CancelReasonCode getReason() {
		return reason;
	}

	public abstract ErrorType getErrorType();
}

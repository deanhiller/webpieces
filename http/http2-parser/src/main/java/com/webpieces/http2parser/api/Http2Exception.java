package com.webpieces.http2parser.api;

import java.util.concurrent.CompletionException;

public abstract class Http2Exception extends CompletionException {

    private static final long serialVersionUID = -2704718008204232741L;
    private int streamId = 0x0;
	private ParseFailReason reason;

    public Http2Exception(ParseFailReason reason, int streamId, String msg) {
        super(msg+" reason="+reason+" stream="+streamId);
		this.reason = reason;
		this.streamId = streamId;
    }

    public Http2Exception(ParseFailReason reason, int streamId, String msg, Throwable e) {
        super(msg+" reason="+reason+" stream="+streamId, e);
        this.reason = reason;
		this.streamId = streamId;
        
    }

    public int getStreamId() {
        return streamId;
    }

    public boolean hasStream() {
        return streamId == 0x0;
    }

	public ParseFailReason getReason() {
		return reason;
	}

	public abstract ErrorType getErrorType();
}

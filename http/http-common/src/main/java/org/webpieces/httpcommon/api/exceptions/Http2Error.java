package org.webpieces.httpcommon.api.exceptions;

import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public abstract class Http2Error extends RuntimeException {
	public Http2Error() {}
	
    public Http2Error(Throwable e) {
    	super(e);
	}

	public abstract List<Http2Msg> toFrames();
}


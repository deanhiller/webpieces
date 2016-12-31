package org.webpieces.http2client.api;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface Http2ServerListener {

	void incomingControlFrame(Http2Frame lowLevelFrame);

	void farEndClosed(Http2Socket socket);

	void failure(Exception e);
	
}

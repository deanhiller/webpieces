package org.webpieces.http2client.mock;

import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class MockServerListener implements Http2ServerListener {

	@Override
	public void incomingControlFrame(Http2Frame lowLevelFrame) {
	}

	@Override
	public void farEndClosed(Http2Socket socket) {
	}

	@Override
	public void failure(Exception e) {
	}

}

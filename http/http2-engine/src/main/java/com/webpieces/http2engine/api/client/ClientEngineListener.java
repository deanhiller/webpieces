package com.webpieces.http2engine.api.client;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2engine.api.error.ShutdownConnection;

public interface ClientEngineListener {

	void sendControlFrameToClient(Http2Frame lowLevelFrame);

	XFuture<Void> sendToSocket(ByteBuffer newData);

	void engineClosedByFarEnd();

	void closeSocket(ShutdownConnection reason);

}

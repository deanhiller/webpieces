package com.webpieces.http2engine.impl.shared;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.impl.shared.data.Stream;

public interface Synchro {

	XFuture<Void> sendDataToSocket(Stream stream, StreamMsg data);

}

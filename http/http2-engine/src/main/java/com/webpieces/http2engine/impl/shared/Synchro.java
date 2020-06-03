package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2engine.impl.shared.data.Stream;

public interface Synchro {

	CompletableFuture<Void> sendDataToSocket(Stream stream, StreamMsg data);

}

package com.webpieces.http2engine.impl.shared;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.impl.shared.data.Stream;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public interface Synchro {

	CompletableFuture<Void> sendDataToSocket(Stream stream, PartialStream data);

}

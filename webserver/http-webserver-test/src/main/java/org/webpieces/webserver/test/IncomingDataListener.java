package org.webpieces.webserver.test;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface IncomingDataListener {

	CompletableFuture<Void> write(ByteBuffer b);

	CompletableFuture<Void> close();

}

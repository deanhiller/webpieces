package org.webpieces.router.impl.proxyout.filereaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface ChunkReader {

	CompletableFuture<Integer> read(ByteBuffer buf, String filePathForLogging, int position);

	void close() throws IOException;

}

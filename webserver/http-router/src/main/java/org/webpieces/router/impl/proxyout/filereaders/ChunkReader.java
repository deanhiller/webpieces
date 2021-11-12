package org.webpieces.router.impl.proxyout.filereaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

public interface ChunkReader {

	XFuture<Integer> read(ByteBuffer buf, String filePathForLogging, int position);

	void close() throws IOException;

}

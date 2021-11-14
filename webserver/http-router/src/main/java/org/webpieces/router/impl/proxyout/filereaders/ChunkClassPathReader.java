package org.webpieces.router.impl.proxyout.filereaders;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.webpieces.util.exceptions.NioException;
import org.webpieces.util.file.VirtualFile;

public class ChunkClassPathReader implements ChunkReader {

	private InputStream inputStream;
	private VirtualFile fullFilePath;

	public ChunkClassPathReader(InputStream inputStream, VirtualFile fullFilePath) {
		this.inputStream = inputStream;
		this.fullFilePath = fullFilePath;
	}

	@Override
	public String toString() {
		return "ChunkClassPathReader="+fullFilePath;
	}
	
	@Override
	public XFuture<Integer> read(ByteBuffer buf, String filePathForLogging, int position) {
		XFuture<Integer> future = new XFuture<Integer>();

		try {
			if(buf.position() != 0)
				throw new IllegalStateException("buffer is not in a clean state. buf="+buf);
			else if(buf.limit() != buf.capacity())
				throw new IllegalStateException("buffer is not in a clean state. buf="+buf);
				
			int numRead = inputStream.read(buf.array());
			
			if(numRead > 0)
				buf.position(numRead); //update the buffer like someone wrote into the buffer instead of directly into the byte[]
		
			future.complete(numRead);
			return future;
		} catch(IOException e) {
			future.completeExceptionally(new NioException("Excception reading", e));
			return future;
		}
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}

}

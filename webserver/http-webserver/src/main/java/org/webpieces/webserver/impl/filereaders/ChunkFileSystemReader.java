package org.webpieces.webserver.impl.filereaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class ChunkFileSystemReader implements ChunkReader {

	private static final Logger log = LoggerFactory.getLogger(ChunkFileSystemReader.class);

	private AsynchronousFileChannel asyncFile;
	private Path file;
	
	public ChunkFileSystemReader(AsynchronousFileChannel asyncFile, Path file) {
		this.asyncFile = asyncFile;
		this.file = file;
	}
	
	@Override
	public String toString() {
		return "ChunkFileSystemReader="+file;
	}
	
	@Override
	public CompletableFuture<Integer> read(ByteBuffer buf, String filePathForLogging, int position) {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		
		int remaining = buf.remaining();
    
		CompletionHandler<Integer, String> handler = new CompletionHandler<Integer, String>() {
			@Override
			public void completed(Integer result, String attachment) {
				if(result.intValue() == -1 && remaining != buf.remaining()) {
					future.completeExceptionally(new XFileReadException("Async reader returned -1 but apparently wrote some data too.  buf="+buf+" remainingPrevious="+remaining));
				} else {
					future.complete(result);
				}
			}
			@Override
			public void failed(Throwable exc, String attachment) {
				log.error("Failed to read file="+file, exc);
				future.completeExceptionally(new XFileReadException("Failed to read file="+filePathForLogging, exc));
			}
		};
		asyncFile.read(buf, position, "attachment", handler);

		return future;
	}

	public void close() throws IOException {
		asyncFile.close();
	}

	public long length() {
		return file.toFile().length();
	}
}

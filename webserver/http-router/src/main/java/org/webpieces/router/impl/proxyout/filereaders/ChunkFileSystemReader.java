package org.webpieces.router.impl.proxyout.filereaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
	private static class OurCompletionHandler implements CompletionHandler<Integer, String> {
		private CompletableFuture<Integer> future;
		private int remaining;
		private String socket;
		private String txId;
		private String user;
		private Path file;
		private ByteBuffer buf;
		private String filePathForLogging;
		
		public OurCompletionHandler(CompletableFuture<Integer> future, int remaining, Path file, ByteBuffer buf, String filePathForLogging) {
			this.future = future;
			this.remaining = remaining;
			this.file = file;
			this.buf = buf;
			this.filePathForLogging = filePathForLogging;
			socket = MDC.get("svrSocket");
			txId = MDC.get("txId");
			user = MDC.get("userId");
		}
		@Override
		public void completed(Integer result, String attachment) {
			try {
				MDC.put("svrSocket", socket);
				MDC.put("txId", txId);
				MDC.put("userId", user);
				if(result.intValue() == -1 && remaining != buf.remaining()) {
					future.completeExceptionally(new XFileReadException("Async reader returned -1 but apparently wrote some data too.  buf="+buf+" remainingPrevious="+remaining));
				} else {
					future.complete(result);
				}
			} finally {
				MDC.clear();
			}
		}
		@Override
		public void failed(Throwable exc, String attachment) {
			try {
				MDC.put("svrSocket", socket);
				MDC.put("txId", txId);
				MDC.put("userId", user);				
				log.error("Failed to read file="+file, exc);
				future.completeExceptionally(new XFileReadException("Failed to read file="+filePathForLogging, exc));
			} finally {
				MDC.clear();
			}
		}		
	}
	
	@Override
	public CompletableFuture<Integer> read(ByteBuffer buf, String filePathForLogging, int position) {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		
		int remaining = buf.remaining();
    
		OurCompletionHandler handler = new OurCompletionHandler(future, remaining, file, buf, filePathForLogging);
		
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

package org.webpieces.webserver.impl.filereaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.ChannelCloser;
import org.webpieces.webserver.impl.RequestInfo;
import org.webpieces.webserver.impl.ResponseCreator;
import org.webpieces.webserver.impl.ResponseCreator.ResponseEncodingTuple;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public abstract class XFileReader {

	private static final Logger log = LoggerFactory.getLogger(XFileReader.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private final ResponseCreator responseCreator;
	private final WebServerConfig config;
	private final ChannelCloser channelCloser;
	
	public XFileReader(ResponseCreator responseCreator, WebServerConfig config, ChannelCloser channelCloser) {
		super();
		this.responseCreator = responseCreator;
		this.config = config;
		this.channelCloser = channelCloser;
	}

	public CompletableFuture<Void> runFileRead(RequestInfo info, RenderStaticResponse renderStatic) throws IOException {
		VirtualFile fullFilePath = renderStatic.getFilePath();

		String fileName = getNameToUse(fullFilePath);
	    String extension = null;
	    int lastDot = fileName.lastIndexOf(".");
	    if(lastDot > 0) {
	    	extension = fileName.substring(lastDot+1);
	    }

	    ResponseEncodingTuple tuple = responseCreator.createResponse(info.getRequest(),
	    		StatusCode.HTTP_200_OK, extension, "application/octet-stream", false);
	    Http2Response response = tuple.response;

		//On startup, we protect developers from breaking clients.  In http, all files that change
		//must also change the hash automatically and the %%{ }%% tag generates those hashes so the
	    //files loaded are always the latest
	    
		Long timeSeconds = config.getStaticFileCacheTimeSeconds();
		if(timeSeconds != null)
			response.addHeader(new Http2Header(Http2HeaderName.CACHE_CONTROL, "max-age="+timeSeconds));
		
		ChunkReader reader = createFileReader(
				response, renderStatic, fileName, fullFilePath, info, extension, tuple);
		
		CompletableFuture<Void> future;
		try {
			if(log.isDebugEnabled())
				log.debug("sending chunked file via async read="+reader);
			future = info.getResponseSender().sendResponse(response)
					.thenCompose(s -> readLoop(s, info.getPool(), reader, 0));
		} catch(Throwable e) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(e);
		}
		
		return future.handle((s, exc) -> handleClose(info, reader, exc)) //our finally block for failures
					.thenAccept(s -> empty());
	}

	protected abstract String getNameToUse(VirtualFile fullFilePath);

	protected abstract ChunkReader createFileReader(Http2Response response, RenderStaticResponse renderStatic,
			String fileName, VirtualFile fullFilePath, RequestInfo info, String extension,
			ResponseEncodingTuple tuple);

	private void empty() {}

	
	private CompletableFuture<Void> readLoop(
			StreamWriter writer, BufferPool pool, ChunkReader reader, int position) {
		//Because asyncRead creates a new future every time and dumps it to a fileExecutor threadpool, we do not need
		//to use future.thenApplyAsync to avoid a stackoverflow

		ByteBuffer buf = pool.nextBuffer(BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE);

		//NOTE: I don't like inlining code BUT this is recursive and I HATE recursion between multiple methods so
		//this method fileReadLoop ONLY calls itself below as it continues to read and send chunks

		CompletableFuture<Integer> future = reader.read(buf, reader+"", position);

		return future.thenCompose(readCount -> {
					buf.flip();
					int read = buf.remaining();
					if(readCount == 0) {
						throw new XFileReadException("bug in webpieces.  we didn't know it could read 0.  quick fix this one.  readCount="+readCount+" read="+read);
					} else if(readCount == -1) {
						if(read > 0)
							throw new XFileReadException("mismatch, readCount says -1 but bytes read is > 0.  read="+read);
						
						return sendHttpChunk(writer, pool, buf, true);
					} else if(read != readCount) { 
						throw new IllegalStateException("read bytes into buf does not match readCount. read="+read+" cnt="+readCount);
					}

					return sendHttpChunk(writer, pool, buf, false).thenCompose( (w) -> {
						int newPosition = position + read;
						//BIG NOTE: RECURSIVE READ HERE!!!!
						return readLoop(writer, pool, reader, newPosition);	
					});
			});
	}
	
	private CompletableFuture<Void> sendHttpChunk(StreamWriter writer, BufferPool pool, ByteBuffer buf, boolean isEos) {
		DataWrapper data = wrapperFactory.wrapByteBuffer(buf);

		int len = data.getReadableSize();
		if(log.isTraceEnabled())
			log.trace("SENDING data to="+writer+" len="+len+" isEnd="+isEos+" content="+data.createStringFromUtf8(0, len));

		DataFrame frame = new DataFrame();
		frame.setEndOfStream(isEos);
		frame.setData(data);
		return writer.processPiece(frame).thenApply( w -> {
			//at this point, the buffer was consumed
			//after process, release the buffer for re-use
			buf.position(buf.limit());
			
			pool.releaseBuffer(buf);
			return null;
		});
	}
	
	private Void handleClose(RequestInfo info, ChunkReader reader, Throwable exc) {

		//now we close if needed
		try {
			channelCloser.closeIfNeeded(info.getRequest(), info.getResponseSender());
		} catch(Throwable e) {
			if(exc == null) //Previous exception more important so only log if no previous exception
				log.error("Exception closing if needed", e);
		}
		
		try {
			reader.close();
		} catch(Throwable e) {
			if(exc == null) //Previous exception way more important so ignore this until they fix previous exception
				log.error("Exception closing reader", e);
		}
		
		if(exc != null) {
			if(exc instanceof NioClosedChannelException)
				throw (NioClosedChannelException)exc;
			else 
				throw new ReadOrSendException(exc);
		}
		return null;
	}

}

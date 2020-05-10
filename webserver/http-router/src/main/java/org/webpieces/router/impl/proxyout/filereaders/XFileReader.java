package org.webpieces.router.impl.proxyout.filereaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.ProxyStreamHandle;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ChannelCloser;
import org.webpieces.router.impl.proxyout.ResponseCreator;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.file.VirtualFile;

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
	private final RouterConfig config;
	private final ChannelCloser channelCloser;
	
	public XFileReader(ResponseCreator responseCreator, RouterConfig config, ChannelCloser channelCloser) {
		super();
		this.responseCreator = responseCreator;
		this.config = config;
		this.channelCloser = channelCloser;
	}

	public CompletableFuture<Void> runFileRead(RequestInfo info, RenderStaticResponse renderStatic, ProxyStreamHandle handle) throws IOException {
		
		
		VirtualFile fullFilePath = renderStatic.getFilePath();

		String fileName = getNameToUse(fullFilePath);
	    String extension = null;
	    int lastDot = fileName.lastIndexOf(".");
	    if(lastDot > 0) {
	    	extension = fileName.substring(lastDot+1);
	    }

	    ResponseCreator.ResponseEncodingTuple tuple = responseCreator.createResponse(info.getRequest(),
	    		StatusCode.HTTP_200_OK, extension, "application/octet-stream", false);
	    Http2Response response = tuple.response;

		//On startup, we protect developers from breaking clients.  In http, all files that change
		//must also change the hash automatically and the %%{ }%% tag generates those hashes so the
	    //files loaded are always the latest
	    
		Long timeSeconds = config.getStaticFileCacheTimeSeconds();
		if(timeSeconds != null)
			response.addHeader(new Http2Header(Http2HeaderName.CACHE_CONTROL, "max-age="+timeSeconds));
		
		ChunkReader reader = createFileReader(
				response, renderStatic, fileName, fullFilePath, info, extension, tuple, handle);
		
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
			ResponseCreator.ResponseEncodingTuple tuple, ProxyStreamHandle handle);

	private void empty() {}

	
	private CompletableFuture<Void> readLoop(
			StreamWriter writer, BufferPool pool, ChunkReader reader, int position) {
		//Because asyncRead creates a new future every time and dumps it to a fileExecutor threadpool, we do not need
		//to use future.thenApplyAsync to avoid a stackoverflow

		//It will grab a MUCH bigger buffer like 5k BUT the point is to RELY on the configuration of the ByteBuffer pool here
		//so if they make it bigger or smaller, we get that size of ByteBuffers as long as it is 1000 or larger
		ByteBuffer buf = pool.nextBuffer(1000);

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

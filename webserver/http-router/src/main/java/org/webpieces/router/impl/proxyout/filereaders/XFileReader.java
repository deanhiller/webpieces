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
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.proxyout.ResponseCreator;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import org.webpieces.http.StatusCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.StreamWriter;

public abstract class XFileReader {

	private static final Logger log = LoggerFactory.getLogger(XFileReader.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	private final ResponseCreator responseCreator;
	private final RouterConfig config;
	private FutureHelper futureUtil;
	
	public XFileReader(
			ResponseCreator responseCreator, 
			RouterConfig config, 
			FutureHelper futureUtil
	) {
		super();
		this.responseCreator = responseCreator;
		this.config = config;
		this.futureUtil = futureUtil;
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

		if(log.isDebugEnabled())
			log.debug("sending chunked file via async read="+reader);
		
		ProxyStreamHandle stream = info.getResponseSender();
		return futureUtil.finallyBlock(
				() -> stream.process(response).thenCompose(s -> readLoop(s, info.getPool(), reader, 0)), 
				() -> handleClose(info, reader)
		);
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
	
	private Void handleClose(RequestInfo info, ChunkReader reader) {		
		try {
			reader.close();
		} catch(Throwable e) {
			throw new ReadOrSendException(e);
		}
		
		return null;
	}

}

package org.webpieces.webserver.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpFrontendFactory;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.ResponseCreator.ResponseEncodingTuple;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

@Singleton
public class StaticFileReader {

	private static final Logger log = LoggerFactory.getLogger(StaticFileReader.class);
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	//TODO: RouterConfig doesn't really belong here but this class is sneaking past
	//the router api to access some stuff it shouldn't right now because I was 
	//lazy (and should really use verify design to prevent things like that).  router also uses the same state this
	//class needs
	@Inject
	private RouterConfig routerConfig;
	@Inject
	private WebServerConfig config;
	@Inject
	@Named(HttpFrontendFactory.FILE_READ_EXECUTOR)
	private ExecutorService fileExecutor;
	@Inject
	private CompressionLookup compressionLookup;
	@Inject
	private ResponseCreator responseCreator;
	@Inject
	private ChannelCloser channelCloser;
	
	private Set<OpenOption> options = new HashSet<>();

	public StaticFileReader() {
	    options.add(StandardOpenOption.READ);
	}
	
	public CompletableFuture<Void> sendRenderStatic(RequestInfo info, RenderStaticResponse renderStatic) {
		try {
			if(renderStatic.isOnClassPath())
				return runClassPathRead(info, renderStatic);
			return runAsyncFileRead(info, renderStatic);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CompletableFuture<Void> runClassPathRead(RequestInfo info, RenderStaticResponse renderStatic) throws IOException {
		VirtualFile fullFilePath = renderStatic.getFilePath();
	    
		URL url = fullFilePath.toURL();
		String fileName = fullFilePath.getAbsolutePath();
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
	    
		Long timeMs = config.getStaticFileCacheTimeSeconds();
		if(timeMs != null)
			response.addHeader(new Http2Header(Http2HeaderName.CACHE_CONTROL, "max-age="+timeMs));

		log.info(()->"sending classpath resource="+fullFilePath);

		InputStream inputStream = fullFilePath.openInputStream();
		CompletableFuture<Void> future = info.getResponseSender().sendResponse(response)
					.thenCompose(s -> classpathReadLoop(s, inputStream, fullFilePath, info));
		
		return future.handle((s, exc) -> handleClose(info, exc)); //our finally block for failures
	}
	
	private CompletableFuture<Void> classpathReadLoop(StreamWriter writer, InputStream inputStream, VirtualFile fullFilePath, RequestInfo info) {
		try {
			byte[] byteArray = new byte[BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE];
			int bytesRead = inputStream.read(byteArray);
			
			DataWrapper data;
			boolean isEos;
			if(bytesRead < 0) {
				data = wrapperFactory.emptyWrapper();
				isEos = true;
			} else {
				data = wrapperFactory.wrapByteArray(byteArray, 0, bytesRead);
				isEos = false;
			}
	
			DataFrame frame = new DataFrame();
			frame.setEndOfStream(isEos);
			frame.setData(data);
			return writer.processPiece(frame).thenCompose(v -> {
				if(isEos)
					return CompletableFuture.completedFuture(null);
				else
					return classpathReadLoop(writer, inputStream, fullFilePath, info);
			});
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CompletableFuture<Void> runAsyncFileRead(RequestInfo info, RenderStaticResponse renderStatic) throws IOException {
		VirtualFile fullFilePath = renderStatic.getFilePath();
	    
		String fileName = fullFilePath.getName();
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
	    
		Long timeMs = config.getStaticFileCacheTimeSeconds();
		if(timeMs != null)
			response.addHeader(new Http2Header(Http2HeaderName.CACHE_CONTROL, "max-age="+timeMs));
		
		Path file;
		Compression compr = compressionLookup.createCompressionStream(info.getRouterRequest().encodings, extension, tuple.mimeType);
		//since we do compression of all text files on server startup, we only support the compression that was used
		//during startup as I don't feel like paying a cpu penalty for compressing while live
	    if(compr != null && compr.getCompressionType().equals(routerConfig.getStartupCompression())) {
		    	response.addHeader(new Http2Header(Http2HeaderName.CONTENT_ENCODING, compr.getCompressionType()));
		    	File routesCache = renderStatic.getTargetCache();
	
		    	String relativeUrl = renderStatic.getRelativeUrl();
		    	File fileReference;
		    	if(relativeUrl == null) {
		    	    fileReference = FileFactory.newFile(routesCache, fileName);
		    	} else {
		    		fileReference = FileFactory.newFile(routesCache, relativeUrl);
		    	}
		    	
		    	file = fetchFile("Compressed File from cache=", fileReference.getAbsolutePath()+".gz");
	    } else {
	    		file = fetchFile("File=", fullFilePath.getAbsolutePath());
	    }

		AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(file, options, fileExecutor);

		CompletableFuture<Void> future;
		try {
			log.info(()->"sending chunked file via async read="+file);
			long length = file.toFile().length();
			AtomicLong remaining = new AtomicLong(length);

			future = info.getResponseSender().sendResponse(response)
					.thenCompose(s -> readLoop(s, info.getPool(), file, asyncFile, 0, remaining));
		} catch(Throwable e) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(e);
		}
		
		return future.handle((s, exc) -> handleClose(info, exc)) //our finally block for failures
					.thenAccept(s -> empty());
	}

	private void empty() {}
	
	private Path fetchFile(String msg, String fullFilePath) {
		Path file = Paths.get(fullFilePath);
		File f = file.toFile();
		if(!f.exists() || !f.isFile())
			throw new NotFoundException(msg+file+" was not found");
		return file;
	}

	private Void handleClose(RequestInfo info, Throwable exc) {

		//now we close if needed
		try {
			channelCloser.closeIfNeeded(info.getRequest(), info.getResponseSender());
		} catch(Throwable e) {
			if(exc == null) //Previous exception more important so only log if no previous exception
				log.error("Exception closing if needed", e);
		}
		
		if(exc != null)
			throw new RuntimeException(exc);
		return null;
	}
	
	private CompletableFuture<Void> readLoop(
			StreamWriter writer, BufferPool pool, Path file, AsynchronousFileChannel asyncFile, int position,
			AtomicLong remaining) {
		//Because asyncRead creates a new future every time and dumps it to a fileExecutor threadpool, we do not need
		//to use future.thenApplyAsync to avoid a stackoverflow

		ByteBuffer buf = pool.nextBuffer(BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE);

		//NOTE: I don't like inlining code BUT this is recursive and I HATE recursion between multiple methods so
		//this method ONLY calls itself below as it continues to read and send chunks

		CompletableFuture<Integer> future = asyncRead(buf, file, asyncFile, position);

		return future.thenCompose(readCount -> {
					buf.flip();
					int read = buf.remaining();
					long bytesLeft = remaining.addAndGet(-read);
					if(read != readCount)
						throw new IllegalStateException("read bytes into buf does not match readCount. read="+read+" cnt="+readCount);
					else if(bytesLeft == 0) {
						return sendHttpChunk(writer, pool, buf, true);
					}

					return sendHttpChunk(writer, pool, buf, false).thenCompose( (w) -> {
						int newPosition = position + read;
						//BIG NOTE: RECURSIVE READ HERE!!!!
						return readLoop(writer, pool, file, asyncFile, newPosition, remaining);								
					});
			});
	}

	private CompletableFuture<Integer> asyncRead(ByteBuffer buf, Path file, AsynchronousFileChannel asyncFile, long position) {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
    
		CompletionHandler<Integer, String> handler = new CompletionHandler<Integer, String>() {
			@Override
			public void completed(Integer result, String attachment) {
				future.complete(result);
			}

			@Override
			public void failed(Throwable exc, String attachment) {
				log.error("Failed to read file="+file, exc);
				future.completeExceptionally(exc);
			}
		};
		asyncFile.read(buf, position, "attachment", handler);

		return future;
	}
	
	private CompletableFuture<Void> sendHttpChunk(StreamWriter writer, BufferPool pool, ByteBuffer buf, boolean isEos) {
		DataWrapper data = wrapperFactory.wrapByteBuffer(buf);
		
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
}

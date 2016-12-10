package org.webpieces.webserver.impl;

import java.io.File;
import java.io.IOException;
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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.ResponseCreator.ResponseEncodingTuple;

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
	@Named(WebServerModule.FILE_READ_EXECUTOR)
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
		if(renderStatic.isOnClassPath())
			throw new UnsupportedOperationException("not implemented yet");
		
		try {
			return runAsyncFileRead(info, renderStatic);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CompletableFuture<Void> runAsyncFileRead(RequestInfo info, RenderStaticResponse renderStatic) throws IOException {
		boolean isFile = true;
		String fullFilePath = renderStatic.getFilePath();
		if(fullFilePath == null) {
			isFile = false;
			fullFilePath = renderStatic.getDirectory()+renderStatic.getRelativePath();
		}
	    
	    String extension = null;
	    int lastDirIndex = fullFilePath.lastIndexOf("/");
	    int lastDot = fullFilePath.lastIndexOf(".");
	    if(lastDot > lastDirIndex) {
	    	extension = fullFilePath.substring(lastDot+1);
	    }
	    	    
	    ResponseEncodingTuple tuple = responseCreator.createResponse(info.getRequest(), KnownStatusCode.HTTP_200_OK, extension, "application/octet-stream");
	    HttpResponse response = tuple.response;

		// we shouldn't have to add chunked because the responseSender will add chunked for us
		// if isComplete is false

		// response.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		//On startup, we protect developers from breaking clients.  In http, all files that change
		//must also change names/url so that clients automatically get the new version right away.
		//This also means, we set the cache time to one year and browsers will automatically evict 
		//when cache size is too large.  We only protect on text file changes not images so expire
		//the cache monthly in case images change without changing names.
		
		Long timeMs = config.getStaticFileCacheTimeSeconds();
		if(timeMs != null)
			response.addHeader(new Header(KnownHeaderName.CACHE_CONTROL, "max-age="+timeMs));
		
		Path file;
		Compression compr = compressionLookup.createCompressionStream(info.getRouterRequest().encodings, extension, tuple.mimeType);
		//since we do compression of all text files on server startup, we only support the compression that was used
		//during startup as I don't feel like paying a cpu penalty for compressing while live
	    if(compr != null && compr.getCompressionType().equals(routerConfig.getStartupCompression())) {
	    	response.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, compr.getCompressionType()));
	    	File dir = routerConfig.getCachedCompressedDirectory();
	    	File routesCache = new File(dir, renderStatic.getStaticRouteId()+"");
	    	
	    	File fileReference;
	    	if(isFile) {
	    	    String fileName = fullFilePath.substring(lastDirIndex+1);
	    	    fileReference = new File(routesCache, fileName);
	    	} else {
	    		fileReference = new File(routesCache, renderStatic.getRelativePath());
	    	}
	    	
	    	fullFilePath = fileReference.getAbsolutePath();
	    	file = fetchFile("Compressed File from cache=", fullFilePath+".gz");
	    } else {
	    	file = fetchFile("File=", fullFilePath);
	    }

		AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(file, options, fileExecutor);

		try {
			log.debug(()->"sending chunked file via async read="+file);
			return info.getResponseSender().sendResponse(response, info.getRequest(), info.getRequestId(), false)
					.thenAccept(responseId -> info.setResponseId(responseId))
					.thenCompose(v -> readLoop(info, file, asyncFile, 0))
					.handle((s, exc) -> handleClose(info, s, exc)) //our finally block for failures
					.thenAccept(s -> empty());
		} catch(Throwable e) {
			//cannot do this on success since it is completing on another thread...
			handleClose(info, true, null);
			throw new RuntimeException(e);
		}
	}

	private void empty() {}
	
	private Path fetchFile(String msg, String fullFilePath) {
		Path file = Paths.get(fullFilePath);
		File f = file.toFile();
		if(!f.exists() || !f.isFile())
			throw new NotFoundException(msg+file+" was not found");
		return file;
	}

	private Boolean handleClose(RequestInfo info, Boolean s, Throwable exc) {

		//now we close if needed
		try {
			channelCloser.closeIfNeeded(info.getRequest(), info.getResponseSender());
		} catch(Throwable e) {
			if(exc == null) //Previous exception more important so only log if no previous exception
				log.error("Exception closing if needed", e);
		}
		
		if(s != null)
			return s;
		else if(exc != null)
			throw new RuntimeException(exc);
		else {
			log.error("oh crap, big bug");
			throw new RuntimeException("This is really bizarre to get here");
		}		
	}
	
	private CompletableFuture<Boolean> readLoop(RequestInfo info, Path file, AsynchronousFileChannel asyncFile, int position) {
		//Because asyncRead creates a new future every time and dumps it to a fileExecutor threadpool, we do not need
		//to use future.thenApplyAsync to avoid a stackoverflow
		CompletableFuture<ByteBuffer> future = asyncRead(info, file, asyncFile, position);
		//NOTE: I don't like inlining code BUT this is recursive and I HATE recursion between multiple methods so
		//this method ONLY calls itself below as it continues to read and send chunks
		return future.thenApply(buf -> {
							buf.flip();
							int read = buf.remaining();
							if(read == 0) {
								sendLastChunk(info, buf);
								return null;
							}

							sendHttpChunk(info, buf);

							int newPosition = position + read;
							//BIG NOTE: RECURSIVE READ HERE!!!! but futures and thenApplyAsync prevent stackoverflow 100%
							readLoop(info, file, asyncFile, newPosition);
							return null;
					})
					.thenApply(s -> true);
	}

	private void sendLastChunk(RequestInfo info, ByteBuffer buf) {
		info.getPool().releaseBuffer(buf);
		info.getResponseSender().sendData(wrapperFactory.emptyWrapper(), info.getResponseId(), true);
	}

	private CompletableFuture<ByteBuffer> asyncRead(RequestInfo info, Path file, AsynchronousFileChannel asyncFile, long position) {
		CompletableFuture<ByteBuffer> future = new CompletableFuture<ByteBuffer>();
    
		ByteBuffer buf = info.getPool().nextBuffer(BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE);

		CompletionHandler<Integer, String> handler = new CompletionHandler<Integer, String>() {
			@Override
			public void completed(Integer result, String attachment) {
				future.complete(buf);
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
	
	private void sendHttpChunk(RequestInfo info, ByteBuffer buf) {
		DataWrapper data = wrapperFactory.wrapByteBuffer(buf);
		
		log.trace(()->"sending chunk with body size="+data.getReadableSize());
		info.getResponseSender().sendData(data, info.getResponseId(), false);
	}
}

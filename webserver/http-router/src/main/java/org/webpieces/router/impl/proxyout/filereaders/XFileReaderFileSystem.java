package org.webpieces.router.impl.proxyout.filereaders;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.webpieces.ctx.api.Constants;
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.ProxyStreamHandle;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.proxyout.ChannelCloser;
import org.webpieces.router.impl.proxyout.ResponseCreator;
import org.webpieces.router.impl.proxyout.ResponseCreator.ResponseEncodingTuple;
import org.webpieces.util.exceptions.NioException;
import org.webpieces.util.file.FileFactory;
import org.webpieces.util.file.VirtualFile;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class XFileReaderFileSystem extends XFileReader {

	//TODO: RouterConfig doesn't really belong here but this class is sneaking past
	//the router api to access some stuff it shouldn't right now because I was 
	//lazy (and should really use verify design to prevent things like that).  router also uses the same state this
	//class needs
	protected final RouterConfig routerConfig;
	private final CompressionLookup compressionLookup;
	private final ExecutorService fileExecutor;
	private final Set<OpenOption> options = new HashSet<>();
	
	@Inject
	public XFileReaderFileSystem(
		ResponseCreator responseCreator, 
		ChannelCloser channelCloser,
		RouterConfig routerConfig, 
		CompressionLookup compressionLookup, 
		@Named(Constants.FILE_READ_EXECUTOR) ExecutorService fileExecutor
	) {
		super(responseCreator, routerConfig, channelCloser);
		this.routerConfig = routerConfig;
		this.compressionLookup = compressionLookup;
		this.fileExecutor = fileExecutor;

	    options.add(StandardOpenOption.READ);
	}
	
	protected ChunkFileSystemReader createFileReader(
		Http2Response response, 
		RenderStaticResponse renderStatic, 
		String fileName, 
		VirtualFile fullFilePath, 
		RequestInfo info, 
		String extension, 
		ResponseEncodingTuple tuple,
		ProxyStreamHandle handle
	) {
		
		Path file;
		Compression compr = compressionLookup.createCompressionStream(
				info.getRouterRequest().encodings, extension, tuple.mimeType);
		//since we do compression of all text files on server startup, we only support the compression that was used
		//during startup as I don't feel like paying a cpu penalty for compressing while live
	    if(compr != null && compr.getCompressionType().equals(routerConfig.getStartupCompression())) {
	    	
	    	handle.setPrecompressedStream(true);
	    	
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

		AsynchronousFileChannel asyncFile;
		try {
			asyncFile = AsynchronousFileChannel.open(file, options, fileExecutor);
			ChunkFileSystemReader reader = new ChunkFileSystemReader(asyncFile, file);
			return reader;
		} catch (IOException e) {
			throw new NioException("Open Channel Exception "+file, e);
		}

	}
	
	private Path fetchFile(String msg, String fullFilePath) {
		Path file = Paths.get(fullFilePath);
		File f = file.toFile();
		if(!f.exists() || !f.isFile())
			throw new NotFoundException(msg+file+" was not found");
		return file;
	}

	@Override
	protected String getNameToUse(VirtualFile fullFilePath) {
		return fullFilePath.getName();
	}
}

package org.webpieces.router.impl.proxyout;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Constants;
import org.webpieces.ctx.api.MissingPropException;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.exceptions.ControllerPageArgsException;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.exceptions.WebSocketClosedException;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.RenderStaticResponse;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.proxyout.ResponseCreator.ResponseEncodingTuple;
import org.webpieces.router.impl.proxyout.filereaders.RequestInfo;
import org.webpieces.router.impl.proxyout.filereaders.StaticFileReader;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

//MUST NOT BE @Singleton!!! since this is created per request
public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	private final TemplateApi templatingService;
	private final StaticFileReader reader;
	private final CompressionLookup compressionLookup;
	private final ResponseCreator responseCreator;
	private final ChannelCloser channelCloser;
	private final BufferPool pool;
	
	private ResponseOverrideSender stream;
	//private HttpRequest request;
	private RouterRequest routerRequest;
	private Http2Request request;

	private int maxBodySize;

	private FutureHelper futureUtil;

	@Inject
	public ProxyResponse(
		TemplateApi templatingService, 
		StaticFileReader reader,
		CompressionLookup compressionLookup, 
		ResponseCreator responseCreator, 
		ChannelCloser channelCloser,
		BufferPool pool,
		FutureHelper futureUtil
	) {
		super();
		this.templatingService = templatingService;
		this.reader = reader;
		this.compressionLookup = compressionLookup;
		this.responseCreator = responseCreator;
		this.channelCloser = channelCloser;
		this.pool = pool;
		this.futureUtil = futureUtil;
	}

	public void init(RouterRequest req, RouterStreamHandle responseSender, int maxBodySize) {
		this.routerRequest = req;
		this.request = req.orginalRequest;
		this.maxBodySize = maxBodySize;
		this.stream = new ResponseOverrideSender(responseSender);
	}

	@Override
	public CompletableFuture<Void> sendRedirect(RedirectResponse httpResponse) {
		if(log.isDebugEnabled())
			log.debug("Sending redirect response. req="+request);
		Http2Response response = responseCreator.createRedirect(request, httpResponse);
		
		log.info("sending REDIRECT response responseSender="+ stream);
		return stream.sendResponse(response).thenApply(w -> {
			channelCloser.closeIfNeeded(request, stream);
			return null;
		});
	}

	@Override
	public CompletableFuture<Void> sendRenderHtml(RenderResponse resp) {
		if(log.isInfoEnabled())
		log.info("About to send render html response for request="+request+" controller="
						+resp.view.getControllerName()+"."+resp.view.getMethodName());
		View view = resp.view;
		String packageStr = view.getPackageName();
		//For this type of View, the template is the name of the method..
		String templateClassName = view.getRelativeOrAbsolutePath();
		int lastIndexOf = templateClassName.lastIndexOf(".");
		String extension = null;
		if(lastIndexOf > 0) {
			extension = templateClassName.substring(lastIndexOf+1);
		}
		
		String templatePath = templateClassName;
		if(!templatePath.startsWith("/")) {
			//relative path so need to form absolute path...
			if(lastIndexOf > 0) {
				templateClassName = templateClassName.substring(0, lastIndexOf);
			}
			templatePath = getTemplatePath(packageStr, templateClassName, extension);
		}
		
		//TODO: stream this out with chunked response instead??....
		StringWriter out = new StringWriter();
		
		try {
			templatingService.loadAndRunTemplate(templatePath, out, resp.pageArgs);
		} catch(MissingPropException e) {
			Set<String> keys = resp.pageArgs.keySet();
			throw new ControllerPageArgsException("Controller.method="+view.getControllerName()+"."+view.getMethodName()+" did\nnot"
					+ " return enough arguments for the template ="+templatePath+".  specifically, the method\nreturned these"
					+ " arguments="+keys+"  There is a chance in your html you forgot the '' around a variable name\n"
							+ "such as #{set 'key'}# but you put #{set key}# which is 'usually' not the correct way\n"
							+ "The missing properties are as follows....\n"+e.getMessage(), e);
		}
		
		String content = out.toString();
		
		StatusCode statusCode;
		switch(resp.routeType) {
		case HTML:
			statusCode = StatusCode.HTTP_200_OK;
			break;
		case NOT_FOUND:
			statusCode = StatusCode.HTTP_404_NOTFOUND;
			break;
		case INTERNAL_SERVER_ERROR:
			statusCode = StatusCode.HTTP_500_INTERNAL_SVR_ERROR;
			break;
		default:
			throw new IllegalStateException("did add case for state="+resp.routeType);
		}
		
		//NOTE: These are ALL String templates, so default the mimeType to text/plain
		//The real mime type is looked up based on extension so htm or html results in text/html
		if(extension == null) {
			extension = "txt";
		}

		String finalExt = extension;
		
		return futureUtil.catchBlockWrap(
				 () -> createResponseAndSend(statusCode, content, finalExt, "text/plain"), 
				 (t) -> convert(t));
	}
	
	private Throwable convert(Throwable t) {
		if(t instanceof NioClosedChannelException)
			//router does not know about the nio layer but it knows about WebSocketClosedException
			//so throw this as a flag to it that it doesn't need to keep trying error pages
			return new WebSocketClosedException("Socket is already closed", t);
		else
			return t;
	}
	
	
	@Override
	public CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic) {
		if(log.isDebugEnabled())
			log.debug("Sending render static html response. req="+request);
		RequestInfo requestInfo = new RequestInfo(routerRequest, request, pool, stream);
		return futureUtil.catchBlockWrap(
			() -> reader.sendRenderStatic(requestInfo, renderStatic), 
			(t) -> convert(t)
		);
	}
	
	private String getTemplatePath(String packageStr, String templateClassName, String extension) {
		String className = templateClassName;
		if(!"".equals(packageStr))
			className = packageStr+"."+className;
		if(!"".equals(extension))
			className = className+"_"+extension;
		
		return templatingService.convertTemplateClassToPath(className);
	}

	@Override
	public CompletableFuture<Void> sendRenderContent(RenderContentResponse resp) {
		ResponseEncodingTuple tuple = responseCreator.createContentResponse(request, resp.getStatusCode(), resp.getReason(), resp.getMimeType());
		return maybeCompressAndSend(null, tuple, resp.getPayload()); 
	}
	
	public CompletableFuture<Void> createResponseAndSend(StatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		ResponseEncodingTuple tuple = responseCreator.createResponse(request, statusCode, extension, defaultMime, true);
		
		if(log.isDebugEnabled())
			log.debug("content about to be sent back="+content);
		
		Charset encoding = tuple.mimeType.htmlResponsePayloadEncoding;
		byte[] bytes = content.getBytes(encoding);
		
		return maybeCompressAndSend(extension, tuple, bytes);
	}

	private CompletableFuture<Void> maybeCompressAndSend(String extension, ResponseEncodingTuple tuple, byte[] bytes) {
		Compression compression = compressionLookup.createCompressionStream(routerRequest.encodings, extension, tuple.mimeType);
		
		Http2Response resp = tuple.response;

		if(bytes.length == 0) {
			resp.setEndOfStream(true);
			return stream.sendResponse(resp).thenApply(w -> null);
		}

		return sendChunkedResponse(resp, bytes, compression);
	}

	private CompletableFuture<Void> sendChunkedResponse(Http2Response resp, byte[] bytes, final Compression compression) {
				
		boolean compressed = false;
		Compression usingCompression;
		if(compression == null) {
			usingCompression = new NoCompression();
		} else {
			usingCompression = compression;
			compressed = true;
			resp.addHeader(new Http2Header(Http2HeaderName.CONTENT_ENCODING, usingCompression.getCompressionType()));
		}

		log.info("sending RENDERHTML response. size="+bytes.length+" code="+resp+" for domain="+routerRequest.domain+" path"+routerRequest.relativePath+" responseSender="+ stream);

		boolean isCompressed = compressed;

		// Send the headers and get the responseid.
		return stream.sendResponse(resp).thenCompose(writer -> {

			List<DataFrame> frames = possiblyCompress(bytes, usingCompression, isCompressed);
			
			CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

			for(int i = 0; i < frames.size(); i++) {
				DataFrame f = frames.get(i);
				if(i == frames.size()-1)
					f.setEndOfStream(true);
				
				future = future.thenCompose(v -> {
					return writer.processPiece(f);
				});
			}
			
			return future;
			
		}).thenApply(w -> null);
	}

	private List<DataFrame> possiblyCompress(byte[] bytes, Compression usingCompression, boolean isCompressed) {
		ChunkedStream chunkedStream = new ChunkedStream(maxBodySize, isCompressed);

		try(OutputStream chainStream = usingCompression.createCompressionStream(chunkedStream)) {
			//IF wrapped in compression above(ie. not NoCompression), sending the WHOLE byte[] in comes out in
			//pieces that get sent out as it is being compressed
			//and http chunks are sent under the covers(in ChunkedStream)
			chainStream.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if(!chunkedStream.isClosed())
			throw new IllegalStateException("ChunkedStream should have been closed");
		
		List<DataFrame> frames = chunkedStream.getFrames();
		return frames;
	}

	public CompletableFuture<Void> failureRenderingInternalServerErrorPage(Throwable e) {
		if(log.isDebugEnabled())
			log.debug("Sending failure html response. req="+request);

		//TODO: IF instance of HttpException with a KnownStatusCode, we should actually send that status code
		//TODO: we should actually just render our own internalServerError.html page with styling and we could do that.

		//This is a final failure so we send a webpieces page next (in the future, we should just use a customer static html file if set)
		//This is only if the webapp 500 html page fails as many times it is a template and they could have another bug in that template.
		String html = "<html><head></head><body>This website had a bug, "
				+ "then when rendering the page explaining the bug, well, they hit another bug.  "
				+ "The webpieces platform saved them from sending back an ugly stack trace.  Contact website owner "
				+ "with a screen shot of this page</body></html>";

		return createResponseAndSend(StatusCode.HTTP_500_INTERNAL_SVR_ERROR, html, "html", "text/html");
	}

}


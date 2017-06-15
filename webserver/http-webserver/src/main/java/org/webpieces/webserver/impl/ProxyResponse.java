package org.webpieces.webserver.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderContentResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.templating.impl.tags.BootstrapModalTag;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.ResponseCreator.ResponseEncodingTuple;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

import groovy.lang.MissingPropertyException;

public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	@Inject
	private TemplateService templatingService;
	@Inject
	private WebServerConfig config;
	@Inject
	private StaticFileReader reader;
	@Inject
	private CompressionLookup compressionLookup;
	@Inject
	private ResponseCreator responseCreator;
	@Inject
	private ChannelCloser channelCloser;
	
	private ResponseOverrideSender stream;
	//private HttpRequest request;
	private BufferPool pool;
	private RouterRequest routerRequest;
	private Http2Request request;

	public void init(RouterRequest req, Http2Request requestHeaders, ResponseStream responseSender, BufferPool pool) {
		this.routerRequest = req;
		this.request = requestHeaders;
		this.stream = new ResponseOverrideSender(responseSender);
		this.pool = pool;
	}

	public void sendRedirectAndClearCookie(RouterRequest req, String badCookieName) {
		RedirectResponse httpResponse = new RedirectResponse(false, req.isHttps, req.domain, req.port, req.relativePath);
		Http2Response response = createRedirect(httpResponse);
		
		responseCreator.addDeleteCookie(response, badCookieName);
		
		log.info("sending REDIRECT(due to bad cookie) response responseSender="+ stream);
		stream.sendResponse(response);

		channelCloser.closeIfNeeded(request, stream);
	}
	
	@Override
	public CompletableFuture<Void> sendRedirect(RedirectResponse httpResponse) {
		log.debug(() -> "Sending redirect response. req="+request);
		Http2Response response = createRedirect(httpResponse);

		log.info("sending REDIRECT response responseSender="+ stream);
		return stream.sendResponse(response).thenApply(w -> {
			
			
			channelCloser.closeIfNeeded(request, stream);
			return null;
		});
	}

	private Http2Response createRedirect(RedirectResponse httpResponse) {
		Http2Response response = new Http2Response();

		if(httpResponse.isAjaxRedirect) {
			response.addHeader(new Http2Header(Http2HeaderName.STATUS, BootstrapModalTag.AJAX_REDIRECT_CODE+""));
			response.addHeader(new Http2Header("reason", "Ajax Redirect"));
		} else {
			response.addHeader(new Http2Header(Http2HeaderName.STATUS, StatusCode.HTTP_303_SEEOTHER.getCodeString()));
			response.addHeader(new Http2Header("reason", StatusCode.HTTP_303_SEEOTHER.getReason()));
		}
		
		String url = httpResponse.redirectToPath;
		if(url.startsWith("http")) {
			//do nothing
		} else if(httpResponse.domain != null && httpResponse.isHttps != null) {
			String prefix = "http://";
			if(httpResponse.isHttps)
				prefix = "https://";
			
			String portPostfix = "";
			if(httpResponse.port != 443 && httpResponse.port != 80)
				portPostfix = ":"+httpResponse.port;
				
			url = prefix + httpResponse.domain + portPostfix + httpResponse.redirectToPath;
		} else if(httpResponse.domain != null) {
			throw new IllegalReturnValueException("Controller is returning a domain without returning isHttps=true or"
					+ " isHttps=false so we can form the entire redirect.  Either drop the domain or set isHttps");
		} else if(httpResponse.isHttps != null) {
			throw new IllegalReturnValueException("Controller is returning isHttps="+httpResponse.isHttps+" but there is"
					+ "no domain set so we can't form the full redirect.  Either drop setting isHttps or set the domain");
		}
		
		Http2Header location = new Http2Header(Http2HeaderName.LOCATION, url);
		response.addHeader(location );
		
		responseCreator.addCommonHeaders(request, response, false, true);

		//Firefox requires a content length of 0 on redirect(chrome doesn't)!!!...
		response.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, 0+""));
		return response;
	}

	@Override
	public CompletableFuture<Void> sendRenderHtml(RenderResponse resp) {
		log.info(() -> "Sending render html response. req="+request);
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
		} catch(MissingPropertyException e) {
			Set<String> keys = resp.pageArgs.keySet();
			throw new ControllerPageArgsException("Controller.method="+view.getControllerName()+"."+view.getMethodName()+" did\nnot"
					+ " return enough arguments for the template ="+templatePath+".  specifically, the method\nreturned these"
					+ " arguments="+keys+"  There is a chance in your html you forgot the '' around a variable name\n"
							+ "such as #{set 'key'}# but you put #{set key}# which is 'usually' not the correct way\n"
							+ "The missing properties are as follows....\n"+e.getMessage(), e);
		}
		
		String content = out.toString();
		
		StatusCode statusCode = StatusCode.HTTP_200_OK;
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
		if(extension == null) {
			extension = "txt";
		}

		return createResponseAndSend(statusCode, content, extension, "text/plain");
	}
	
	@Override
	public CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic) {
		log.info(() -> "Sending render static html response. req="+request);
		RequestInfo requestInfo = new RequestInfo(routerRequest, request, pool, stream);
		return reader.sendRenderStatic(requestInfo, renderStatic);
	}
	
	private String getTemplatePath(String packageStr, String templateClassName, String extension) {
		String className = templateClassName;
		if(!"".equals(packageStr))
			className = packageStr+"."+className;
		if(!"".equals(extension))
			className = className+"_"+extension;
		
		return TemplateUtil.convertTemplateClassToPath(className);
	}

	@Override
	public CompletableFuture<Void> sendRenderContent(RenderContentResponse resp) {
		ResponseEncodingTuple tuple = responseCreator.createContentResponse(request, resp.getStatusCode(), resp.getReason(), resp.getMimeType());
		return maybeCompressAndSend(null, tuple, resp.getPayload()); 
	}
	
	private CompletableFuture<Void> createResponseAndSend(StatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		ResponseEncodingTuple tuple = responseCreator.createResponse(request, statusCode, extension, defaultMime, true);
		
		log.debug(()->"content about to be sent back="+content);
		
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
			
			CompletableFuture<StreamWriter> future = CompletableFuture.completedFuture(writer);
			
			
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
		ChunkedStream chunkedStream = new ChunkedStream(config.getMaxBodySize(), isCompressed);

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

	@Override
	public CompletableFuture<Void> failureRenderingInternalServerErrorPage(Throwable e) {
		log.debug(() -> "Sending failure html response. req="+request);

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

//	public void sendFailure(HttpException exc) {
//		log.debug(() -> "Sending failure response. req="+request);
//
//		createResponseAndSend(exc.getStatusCode(), "Something went wrong(are you hacking the system?)", "txt", "text/plain");
//	}

}


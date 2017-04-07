package org.webpieces.webserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.dto.RedirectResponse;
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

import groovy.lang.MissingPropertyException;

public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	//TODO: Actually should inject this so it is swappable.... (never have statics...it's annoying as hell when customizing)...
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
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
	
	private ResponseOverrideSender responseSender;
	//private HttpRequest request;
	private BufferPool pool;
	private RouterRequest routerRequest;
	private HttpRequest request;
	private RequestId requestId;

	public void init(RouterRequest req, ResponseSender responseSender, BufferPool pool, RequestId requestId) {
		this.routerRequest = req;
		this.request = (HttpRequest) req.orginalRequest;
		this.responseSender = new ResponseOverrideSender(responseSender);
		this.pool = pool;
		this.requestId = requestId;
	}

	public void sendRedirectAndClearCookie(RouterRequest req, String badCookieName) {
		RedirectResponse httpResponse = new RedirectResponse(false, req.isHttps, req.domain, req.port, req.relativePath);
		HttpResponse response = createRedirect(httpResponse);
		
		responseCreator.addDeleteCookie(response, badCookieName);
		
		log.info("sending REDIRECT(due to bad cookie) response responseSender="+ responseSender);
		responseSender.sendResponse(response, request, requestId, true);

		channelCloser.closeIfNeeded(request, responseSender);
	}
	
	@Override
	public void sendRedirect(RedirectResponse httpResponse) {
		log.debug(() -> "Sending redirect response. req="+request);
		HttpResponse response = createRedirect(httpResponse);

		log.info("sending REDIRECT response responseSender="+ responseSender);
		responseSender.sendResponse(response, request, requestId, true);

		channelCloser.closeIfNeeded(request, responseSender);
	}

	private HttpResponse createRedirect(RedirectResponse httpResponse) {
		HttpResponseStatus status = new HttpResponseStatus();
		if(httpResponse.isAjaxRedirect) {
			status.setCode(BootstrapModalTag.AJAX_REDIRECT_CODE);
			status.setReason("Ajax redirect");
		} else
			status.setKnownStatus(KnownStatusCode.HTTP_303_SEEOTHER);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
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
		
		Header location = new Header(KnownHeaderName.LOCATION, url);
		response.addHeader(location );
		
		responseCreator.addCommonHeaders(request, response, false);

		//Firefox requires a content length of 0 on redirect(chrome doesn't)!!!...
		response.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, 0+""));
		return response;
	}

	@Override
	public void sendRenderHtml(RenderResponse resp) {
		log.debug(() -> "Sending render html response. req="+request);
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
		
		KnownStatusCode statusCode = KnownStatusCode.HTTP_200_OK;
		switch(resp.routeType) {
		case BASIC:
			statusCode = KnownStatusCode.HTTP_200_OK;
			break;
		case NOT_FOUND:
			statusCode = KnownStatusCode.HTTP_404_NOTFOUND;
			break;
		case INTERNAL_SERVER_ERROR:
			statusCode = KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR;
			break;
		default:
			throw new IllegalStateException("did add case for state="+resp.routeType);
		}
		
		//NOTE: These are ALL String templates, so default the mimeType to text/plain
		if(extension == null) {
			extension = "txt";
		}

		createResponseAndSend(statusCode, content, extension, "text/plain");
	}
	
	@Override
	public CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic) {
		log.debug(() -> "Sending render static html response. req="+request);
		RequestInfo requestInfo = new RequestInfo(routerRequest, request, requestId, pool, responseSender);
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

	private void createResponseAndSend(KnownStatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		ResponseEncodingTuple tuple = responseCreator.createResponse(request, statusCode, extension, defaultMime, false);
		HttpResponse resp = tuple.response;
		
		log.debug(()->"content about to be sent back="+content);
		
		Charset encoding = tuple.mimeType.htmlResponsePayloadEncoding;
		byte[] bytes = content.getBytes(encoding);
		
		Compression compression = compressionLookup.createCompressionStream(routerRequest.encodings, extension, tuple.mimeType);
		
		//This is a cheat sort of since compression can go from 28235 to 4,785 and we are looking at the
		//non-compressed size so stuff like 16k may be sent chunked even though it is only 3k on the outbound path
		//(not really a big deal though)
		if(bytes.length < config.getMaxBodySize()) {
			sendFullResponse(resp, bytes, compression);
			return;
		}

		sendChunkedResponse(resp, bytes, compression);
	}

	private void sendChunkedResponse(HttpResponse resp, byte[] bytes, final Compression compression) {
		
		log.info("sending CHUNKED RENDERHTML response. size="+bytes.length+" code="+resp.getStatusLine().getStatus()+" for domain="+routerRequest.domain+" path"+routerRequest.relativePath+" responseSender="+ responseSender);

		// we shouldn't have to add chunked because the responseSender will add chunked for us
		// if isComplete is false

		// resp.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));

		boolean compressed = false;
		Compression usingCompression;
		if(compression == null) {
			usingCompression = new NoCompression();
		} else {
			usingCompression = compression;
			compressed = true;
			resp.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, usingCompression.getCompressionType()));
		}

		boolean isCompressed = compressed;

		// Send the headers and get the responseid.
		responseSender.sendResponse(resp, request, requestId, false).thenAccept(responseId -> {

			OutputStream chunkedStream = new ChunkedStream(responseSender, config.getMaxBodySize(), isCompressed, responseId);

			try(OutputStream chainStream = usingCompression.createCompressionStream(chunkedStream)) {
				//IF wrapped in compression above(ie. not NoCompression), sending the WHOLE byte[] in comes out in
				//pieces that get sent out as it is being compressed
				//and http chunks are sent under the covers(in ChunkedStream)
				chainStream.write(bytes);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void sendFullResponse(HttpResponse resp, byte[] bytes, Compression compression) {
		if(compression != null) {
			resp.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, compression.getCompressionType()));
			bytes = synchronousCompress(compression, bytes);
		}

		resp.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, bytes.length+""));

		DataWrapper data = wrapperFactory.wrapByteArray(bytes);
		resp.setBody(data);

		log.info("sending FULL RENDERHTML response. code="+resp.getStatusLine().getStatus()+" for domain="+routerRequest.domain+" path="+routerRequest.relativePath+" responseSender="+ responseSender);
		
		responseSender.sendResponse(resp, request, requestId, true);
		
		channelCloser.closeIfNeeded(request, responseSender);
	}
	
	private byte[] synchronousCompress(Compression compression, byte[] bytes) {
		ByteArrayOutputStream str = new ByteArrayOutputStream(bytes.length);
		
		try(OutputStream stream = compression.createCompressionStream(str)) {
			stream.write(bytes);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		return str.toByteArray();
	}

	@Override
	public void failureRenderingInternalServerErrorPage(Throwable e) {
		log.debug(() -> "Sending failure html response. req="+request);

		//TODO: IF instance of HttpException with a KnownStatusCode, we should actually send that status code
		//TODO: we should actually just render our own internalServerError.html page with styling and we could do that.
		
		//This is a final failure so we send a webpieces page next (in the future, we should just use a customer static html file if set)
		//This is only if the webapp 500 html page fails as many times it is a template and they could have another bug in that template.
		String html = "<html><head></head><body>This website had a bug, "
				+ "then when rendering the page explaining the bug, well, they hit another bug.  "
				+ "The webpieces platform saved them from sending back an ugly stack trace.  Contact website owner "
				+ "with a screen shot of this page</body></html>";
		
		createResponseAndSend(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, html, "html", "text/html");
	}

	public void sendFailure(HttpException exc) {
		log.debug(() -> "Sending failure response. req="+request);

		createResponseAndSend(exc.getStatusCode(), "Something went wrong(are you hacking the system?)", "txt", "text/plain");
	}

}

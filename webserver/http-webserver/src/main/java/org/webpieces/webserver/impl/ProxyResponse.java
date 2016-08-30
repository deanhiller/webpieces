package org.webpieces.webserver.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterCookie;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.common.ResponseCookie;
import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpLastChunk;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpResponseStatus;
import org.webpieces.httpparser.api.dto.HttpResponseStatusLine;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpparser.api.subparsers.HeaderPriorityParser;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.webserver.api.WebServerConfig;
import org.webpieces.webserver.impl.MimeTypes.MimeTypeResult;

import groovy.lang.MissingPropertyException;

public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	//TODO: Actually should inject ALL of these so they are swappable.... (never have statics)...
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");
	private static final DataWrapperGenerator wrapperFactory = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private static final HeaderPriorityParser httpSubParser = HttpParserFactory.createHeaderParser();
	
	@Inject
	private RoutingService urlLookup;
	@Inject
	private TemplateService templatingService;
	@Inject
	private CookieTranslator cookieTranslator;
	@Inject
	@Named(WebServerModule.FILE_READ_EXECUTOR)
	private ExecutorService fileExecutor;
	@Inject
	private MimeTypes mimeTypes;
	@Inject
	private WebServerConfig config;
	
	private Set<OpenOption> options = new HashSet<>();

	private FrontendSocket channel;
	private HttpRequest request;
	private BufferPool pool;
	private Compression compression;
	private String compressionType;

	public ProxyResponse() {
	    options.add(StandardOpenOption.READ);
	}
	
	public void init(HttpRequest req, FrontendSocket channel, BufferPool pool, Compression compression, String compressionType) {
		this.request = req;
		this.channel = channel;
		this.pool = pool;
		this.compression = compression;
		this.compressionType = compressionType;
	}

	@Override
	public void sendRedirect(RedirectResponse httpResponse) {
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(KnownStatusCode.HTTP_303_SEEOTHER);
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		String url = httpResponse.redirectToPath;
		
		if(httpResponse.domain != null && httpResponse.isHttps != null) {
			String prefix = "http://";
			if(httpResponse.isHttps)
				prefix = "https://";
			url = prefix + httpResponse.domain + httpResponse.redirectToPath;
		} else if(httpResponse.domain != null) {
			throw new IllegalReturnValueException("Controller is returning a domain without returning isHttps=true or"
					+ " isHttps=false so we can form the entire redirect.  Either drop the domain or set isHttps");
		} else if(httpResponse.isHttps != null) {
			throw new IllegalReturnValueException("Controller is returning isHttps="+httpResponse.isHttps+" but there is"
					+ "no domain set so we can't form the full redirect.  Either drop setting isHttps or set the domain");
		}
		
		Header location = new Header(KnownHeaderName.LOCATION, url);
		response.addHeader(location );
		
		//Firefox requires a content length of 0 (chrome doesn't)!!!...
		addCommonHeaders(status.getKnownStatus(), response, 0);
		
		log.info("sending REDIRECT response channel="+channel);
		channel.write(response);

		closeIfNeeded();
	}

	@Override
	public CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic) {

		if(renderStatic.isOnClassPath())
			throw new UnsupportedOperationException("not implemented yet");
		
		try {
			return runAsyncFileRead(renderStatic);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CompletableFuture<Void> runAsyncFileRead(RenderStaticResponse renderStatic) throws IOException {
		String fileName = renderStatic.getAbsolutePath();
	    Path file = Paths.get(fileName);
	    
	    File f = file.toFile();
	    if(!f.exists() || !f.isFile())
	    	throw new NotFoundException("File="+file+" was not found");
	    
	    String extension = null;
	    int lastDirIndex = fileName.lastIndexOf("/");
	    int lastDot = fileName.lastIndexOf(".");
	    if(lastDot > lastDirIndex) {
	    	extension = fileName.substring(lastDot+1);
	    }
	    
	    HttpResponse response = createResponse(KnownStatusCode.HTTP_200_OK, null, extension, "application/octet-stream");
	    
	    if(compressionType != null) {
	    	response.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, compressionType));
	    }
	    
	    channel.write(response);
	    
	    //NOTE: try with resource is synchronous and won't work here :(
    	AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(file, options, fileExecutor);
    	
    	RequestContext ctx = Current.getContext();
    	try {
    		return read(file, asyncFile, ctx, 0)
    			.handle((s, exc) -> handleClose(s, exc)) //our finally block for failures
    			.thenApply(s -> null);
    	} catch(Throwable e) {
    		//cannot do this on success since it is completing on another thread...
    		handleClose(true, null);
    		throw new RuntimeException(e);
    	}
	}

	private Boolean handleClose(Boolean s, Throwable exc) {

		//now we close if needed
		try {
			closeIfNeeded();
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
	
	private CompletableFuture<Boolean> read(Path file, AsynchronousFileChannel asyncFile, RequestContext ctx, int position) {
		//must be async since it is recursive(prevents stackoverflow)
		return run(file, asyncFile, position)
					.thenApplyAsync(buf -> {
							buf.flip();
							int read = buf.remaining();
							sendBuffer(ctx, buf);
							
							if(read == 0) {
								return null; //we are done reading
							}
							
							int newPosition = position + read;
							//BIG NOTE: RECURSIVE READ HERE!!!! but futures and thenApplyAsync prevent stackoverflow 100%
							read(file, asyncFile, ctx, newPosition);
							return null;
					})
					.thenApply(s -> true);
	}

	private void sendBuffer(RequestContext ctx, ByteBuffer buf) {
		if(buf.remaining() == 0) {
			HttpLastChunk last = new HttpLastChunk();
			pool.releaseBuffer(buf);
			channel.write(last);
			log.info("last chunk.  empty of course meeting spec");
			return;
		}
		
		log.info("wanting to send buffer size="+buf.remaining());

		DataWrapper data = wrapperFactory.wrapByteBuffer(buf);
		if(compression != null) {
			byte[] bytes = data.createByteArray();
			byte[] compressed = compression.compress(bytes);
			data = wrapperFactory.wrapByteArray(compressed);
		}
		
		HttpChunk chunk = new HttpChunk();
		chunk.setBody(data);
		channel.write(chunk);		
	}

	private CompletableFuture<ByteBuffer> run(Path file, AsynchronousFileChannel asyncFile, long position) {
		CompletableFuture<ByteBuffer> future = new CompletableFuture<ByteBuffer>();
    
		ByteBuffer buf = pool.nextBuffer(BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE);

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

	@Override
	public void sendRenderHtml(RenderResponse resp) {
		View view = resp.view;
		String packageStr = view.getPackageName();
		//For this type of View, the template is the name of the method..
		String templateClassName = view.getRelativeOrAbsolutePath();
		int lastIndexOf = templateClassName.lastIndexOf(".");
		String extension = null;
		if(lastIndexOf > 0) {
			extension = templateClassName.substring(lastIndexOf+1);
			templateClassName = templateClassName.substring(0, lastIndexOf);
		}
		
		String templatePath = getTemplatePath(packageStr, templateClassName, extension);
		
		//TODO: get html from the request such that we look up the correct template? AND if not found like they request only json, than
		//we send back a 404 rather than a 500
		Template template = templatingService.loadTemplate(templatePath);

		//TODO: stream this out with chunked response instead??....
		StringWriter out = new StringWriter();
		
		try {
			templatingService.runTemplate(template, out, resp.pageArgs, (id, args) -> urlLookup.convertToUrl(id, args));
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
		
		HttpResponse response = createResponse(statusCode, content, extension, "text/plain");
				
		log.info("sending RENDERHTML response. code="+statusCode+" for path="+request.getRequestLine().getUri().getUri()+" channel="+channel);
		if(log.isDebugEnabled())
			log.debug("content sent back="+content);
		
		channel.write(response);
		
		closeIfNeeded();
	}

	private List<RouterCookie> createCookies(KnownStatusCode statusCode) {
		if(!Current.isContextSet())
			return new ArrayList<>(); //in some exceptional cases like incoming cookies failing to parse, there will be no cookies
		
		try {
			List<RouterCookie> cookies = new ArrayList<>();
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.flash());
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.validation());
			cookieTranslator.addScopeToCookieIfExist(cookies, Current.session());
			return cookies;
		} catch(IllegalStateException e) {
			if(statusCode != KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR)
				throw e;
			//ignore as this happened just a second ago for http 500 OR it doesn't matter as it's a 500 anyways
			return new ArrayList<>();
		}
	}
	
	private String getTemplatePath(String packageStr, String templateClassName, String extension) {
		String className = templateClassName;
		if(!"".equals(packageStr))
			className = packageStr+"."+className;
		if(!"".equals(extension))
			className = className+"_"+extension;
		
		return TemplateUtil.convertTemplateClassToPath(className);
	}

	private HttpResponse createResponse(KnownStatusCode statusCode, String content, String extension, String defaultMime) {
		
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, defaultMime);
		
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(statusCode);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		response.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, mimeType.mime));
		
		Integer length = null;
		if(content != null) {
			Charset encoding = mimeType.htmlResponsePayloadEncoding;
			if(encoding == null)
				encoding = config.getDefaultResponseBodyEncoding();
			byte[] bytes = content.getBytes(encoding);
			if(compression != null && compressionType != null) {
				response.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, compressionType));
				bytes = compression.compress(bytes);
			}
			DataWrapper data = wrapperFactory.wrapByteArray(bytes);
			response.setBody(data);
			length = bytes.length;
		}

		addCommonHeaders(statusCode, response, length);
		return response;
	}

	private void addCommonHeaders(KnownStatusCode statusCode, HttpResponse response, Integer contentLength) {
		
		if(contentLength != null) {
			response.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, contentLength+""));
		} else {
			response.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
		}
		
		Header connHeader = null;
		if(request != null)
			connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		
		DateTime now = DateTime.now().toDateTime(DateTimeZone.UTC);
		String dateStr = formatter.print(now)+" GMT";

		//in general, nearly all these headers are desired..
		Header date = new Header(KnownHeaderName.DATE, dateStr);
		response.addHeader(date);

//		Header xFrame = new Header("X-Frame-Options", "SAMEORIGIN");
//		response.addHeader(xFrame);
		
		List<RouterCookie> cookies = createCookies(statusCode);
		for(RouterCookie c : cookies) {
			Header cookieHeader = create(c);
			response.addHeader(cookieHeader);
		}
		
		//X-XSS-Protection: 1; mode=block
		//X-Frame-Options: SAMEORIGIN
	    //Expires: Mon, 20 Jun 2016 02:33:52 GMT\r\n
	    //Cache-Control: private, max-age=31536000\r\n
	    //Last-Modified: Mon, 02 Apr 2012 02:13:37 GMT\r\n
		//X-Content-Type-Options: nosniff\r\n
		
		if(connHeader == null)
			return;
		else if(!"keep-alive".equals(connHeader.getValue()))
			return;

		//just re-use the connHeader from the request...
		response.addHeader(connHeader);
	}

	private Header create(RouterCookie c) {
		ResponseCookie cookie = new ResponseCookie();
		cookie.setName(c.name);
		cookie.setValue(c.value);
		cookie.setDomain(c.domain);
		cookie.setPath(c.path);
		cookie.setMaxAgeSeconds(c.maxAgeSeconds);
		cookie.setSecure(c.isSecure);
		cookie.setHttpOnly(c.isHttpOnly);
		return httpSubParser.createHeader(cookie);
	}

	private Void closeIfNeeded() {
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		boolean close = false;
		if(connHeader != null) {
			String value = connHeader.getValue();
			if(!"keep-alive".equals(value)) {
				close = true;
			}
		} else
			close = true;
		
		if(close)
			channel.close();
		
		return null;
	}

	@Override
	public void failureRenderingInternalServerErrorPage(Throwable e) {
		
		//TODO: IF instance of HttpException with a KnownStatusCode, we should actually send that status code
		
		//This is a final failure so we send a webpieces page next (in the future, we should just use a customer static html file if set)
		//This is only if the webapp 500 html page fails as many times it is a template and they could have another bug in that template.
		String html = "<html><head></head><body>This website had a bug, "
				+ "then when rendering the page explaining the bug, well, they hit another bug.  "
				+ "The webpieces platform saved them from sending back an ugly stack trace.  Contact website owner "
				+ "with a screen shot of this page</body></html>";
		
		HttpResponse response = createResponse(KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, html, "txt", "text/plain");
		
		channel.write(response);
		
		closeIfNeeded();
	}

	public void sendFailure(HttpException exc) {
		HttpResponse response = createResponse(exc.getStatusCode(), "Something went wrong(are you hacking the system?)", "txt", "text/plain");
		channel.write(response);
		closeIfNeeded();
	}

}

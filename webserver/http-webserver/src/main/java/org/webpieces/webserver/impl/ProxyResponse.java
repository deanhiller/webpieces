package org.webpieces.webserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import org.webpieces.ctx.api.RouterRequest;
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
import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RenderStaticResponse;
import org.webpieces.router.api.dto.View;
import org.webpieces.router.api.exceptions.CookieTooLargeException;
import org.webpieces.router.api.exceptions.IllegalReturnValueException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.impl.CookieTranslator;
import org.webpieces.router.impl.compression.Compression;
import org.webpieces.router.impl.compression.CompressionLookup;
import org.webpieces.router.impl.compression.MimeTypes;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.templating.api.Template;
import org.webpieces.templating.api.TemplateService;
import org.webpieces.templating.api.TemplateUtil;
import org.webpieces.webserver.api.WebServerConfig;

import groovy.lang.MissingPropertyException;

public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("E, dd MMM Y HH:mm:ss");
	
	//TODO: Actually should inject ALL of these so they are swappable.... (never have statics...it's annoying as hell when customizing)...
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
	
	//TODO: RouterConfig doesn't really belong here but this class is sneaking past the router api to access some stuff it shouldn't right
	//now because I was lazy (and should really use verify design to prevent things like that).
	@Inject
	private RouterConfig routerConfig;
	@Inject
	private CompressionLookup compressionLookup;
	
	private Set<OpenOption> options = new HashSet<>();

	private FrontendSocket channel;
	//private HttpRequest request;
	private BufferPool pool;
	private RouterRequest routerRequest;
	private HttpRequest request;

	public ProxyResponse() {
	    options.add(StandardOpenOption.READ);
	}
	
	public void init(RouterRequest req, FrontendSocket channel, BufferPool pool) {
		this.routerRequest = req;
		this.request = (HttpRequest) req.orginalRequest;
		this.channel = channel;
		this.pool = pool;
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
		
		addCommonHeaders(status.getKnownStatus(), response);

		//Firefox requires a content length of 0 on redirect(chrome doesn't)!!!...
		response.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, 0+""));

		log.info("sending REDIRECT response channel="+channel);
		channel.write(response);

		closeIfNeeded();
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

		createResponseAndSend(statusCode, content, extension, "text/plain");
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
	    	    
	    ResponseEncodingTuple tuple = createResponse(KnownStatusCode.HTTP_200_OK, extension, "application/octet-stream");
	    HttpResponse response = tuple.response; 
	    
		response.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
		
		Path file;
		Compression compr = compressionLookup.createCompressionStream(routerRequest.encodings, extension, tuple.mimeType);
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
	    	file = Paths.get(fullFilePath);
		    File f = file.toFile();
		    if(!f.exists() || !f.isFile())
		    	throw new NotFoundException("Compressed File from cache="+file+" was not found");
	    } else {
	    	file = Paths.get(fullFilePath);
		    File f = file.toFile();
		    if(!f.exists() || !f.isFile())
		    	throw new NotFoundException("File="+file+" was not found");
	    }

	    channel.write(response);
	    if(log.isDebugEnabled())
	    	log.debug("sending chunked file via async read="+file);
	    
	    //NOTE: try with resource is synchronous and won't work here :(
	    //Use fileExecutor for the callback so we control threadpool configuration...
    	AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(file, options, fileExecutor);
    	
    	RequestContext ctx = Current.getContext();
    	try {
    		return readLoop(file, asyncFile, ctx, 0)
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
	
	private CompletableFuture<Boolean> readLoop(Path file, AsynchronousFileChannel asyncFile, RequestContext ctx, int position) {
		//Because asyncRead creates a new future every time and dumps it to a fileExecutor threadpool, we do not need
		//to use future.thenApplyAsync to avoid a stackoverflow
		CompletableFuture<ByteBuffer> future = asyncRead(file, asyncFile, position);
		//NOTE: I don't like inlining code BUT this is recursive and I HATE recursion between multiple methods so
		//this method ONLY calls itself below as it continues to read and send chunks
		return future.thenApply(buf -> {
							buf.flip();
							int read = buf.remaining();
							if(read == 0) {
								sendLastChunk(buf);
								return null;
							}

							sendHttpChunk(ctx, buf);

							int newPosition = position + read;
							//BIG NOTE: RECURSIVE READ HERE!!!! but futures and thenApplyAsync prevent stackoverflow 100%
							readLoop(file, asyncFile, ctx, newPosition);
							return null;
					})
					.thenApply(s -> true);
	}

	private void sendLastChunk(ByteBuffer buf) {
		HttpLastChunk last = new HttpLastChunk();
		pool.releaseBuffer(buf);
		channel.write(last);
	}

	private CompletableFuture<ByteBuffer> asyncRead(Path file, AsynchronousFileChannel asyncFile, long position) {
		CompletableFuture<ByteBuffer> future = new CompletableFuture<ByteBuffer>();
    
		ByteBuffer buf = pool.nextBuffer(BufferCreationPool.DEFAULT_MAX_BUFFER_SIZE);

		CompletionHandler<Integer, String> handler = new CompletionHandler<Integer, String>() {
			@Override
			public void completed(Integer result, String attachment) {
				log.info("read completed some reading size(which thread)="+result);
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
	
	private void sendHttpChunk(RequestContext ctx, ByteBuffer buf) {
		DataWrapper data = wrapperFactory.wrapByteBuffer(buf);
		
		log.info("temporary send chunk. size(which thread)="+data.getReadableSize());
		
		if(log.isTraceEnabled())
			log.trace("sending chunk with body size="+data.getReadableSize());
		
		HttpChunk chunk = new HttpChunk();
		chunk.setBody(data);
		channel.write(chunk);
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
		} catch(CookieTooLargeException e) {
			if(statusCode != KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR)
				throw e;
			//ELSE this is the second time we are rendering a response AND it was MOST likely caused by the same
			//thing when we tried to marshal out cookies to strings and they were too big, sooooooooooo in this
			//case, clear the cookie that failed.  One log of this should have already occurred but just in case
			//add one more log here but not with stack trace(so we don't get the annoying double stack trace on
			//failing. (The throws above is logged in catch statement elsewhere)
			log.error("Could not marshal cookie on http 500.  msg="+e.getMessage());
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

	private void createResponseAndSend(KnownStatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		ResponseEncodingTuple tuple = createResponse(statusCode, extension, defaultMime);
		HttpResponse resp = tuple.response;

		if(log.isDebugEnabled())
			log.debug("content about to be sent back="+content);
		
		Charset encoding = tuple.mimeType.htmlResponsePayloadEncoding;
		byte[] bytes = content.getBytes(encoding);
		
		Compression compression = compressionLookup.createCompressionStream(routerRequest.encodings, extension, tuple.mimeType);
		
		if(bytes.length < config.getMaxBodySize()) {
			sendFullResponse(resp, bytes, compression);
			return;
		}

		sendChunkedResponse(resp, bytes, compression);
	}

	private void sendChunkedResponse(HttpResponse resp, byte[] bytes, Compression compression) {
		
		log.info("sending CHUNKED RENDERHTML response. size="+bytes.length+"code="+resp.getStatusLine().getStatus()+" for domain="+routerRequest.domain+" path"+routerRequest.relativePath+" channel="+channel);

		resp.addHeader(new Header(KnownHeaderName.TRANSFER_ENCODING, "chunked"));
		
		OutputStream chunkedStream = new ChunkedStream(channel, config.getMaxBodySize());
		
		if(compression == null) {
			compression = new NoCompression();
		} else {
			resp.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, compression.getCompressionType()));
		}

		channel.write(resp);

		try(OutputStream chainStream = compression.createCompressionStream(chunkedStream)) {
			//IF wrapped in compression above(ie. not NoCompression), sending the WHOLE byte[] in comes out in
			//pieces that get sent out as it is being compressed
			//and http chunks are sent under the covers(in ChunkedStream)
			chainStream.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	private void sendFullResponse(HttpResponse resp, byte[] bytes, Compression compression) {
		if(compression != null) {
			resp.addHeader(new Header(KnownHeaderName.CONTENT_ENCODING, compression.getCompressionType()));
			bytes = synchronousCompress(compression, bytes);
		}

		resp.addHeader(new Header(KnownHeaderName.CONTENT_LENGTH, bytes.length+""));

		DataWrapper data = wrapperFactory.wrapByteArray(bytes);
		resp.setBody(data);

		log.info("sending FULL RENDERHTML response. code="+resp.getStatusLine().getStatus()+" for domain="+routerRequest.domain+" path="+routerRequest.relativePath+" channel="+channel);
		
		channel.write(resp);
		
		closeIfNeeded();
	}
	
	private ResponseEncodingTuple createResponse(KnownStatusCode statusCode, String extension, String defaultMime) {
		MimeTypeResult mimeType = mimeTypes.extensionToContentType(extension, defaultMime);
		
		HttpResponseStatus status = new HttpResponseStatus();
		status.setKnownStatus(statusCode);
		
		HttpResponseStatusLine statusLine = new HttpResponseStatusLine();
		statusLine.setStatus(status);
		HttpResponse response = new HttpResponse();
		response.setStatusLine(statusLine);
		
		response.addHeader(new Header(KnownHeaderName.CONTENT_TYPE, mimeType.mime));

		addCommonHeaders(statusCode, response);
		return new ResponseEncodingTuple(response, mimeType);
	}

	private static class ResponseEncodingTuple {
		public HttpResponse response;
		public MimeTypeResult mimeType;

		public ResponseEncodingTuple(HttpResponse response, MimeTypeResult mimeType) {
			this.response = response;
			this.mimeType = mimeType;
		}	
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

	private void addCommonHeaders(KnownStatusCode statusCode, HttpResponse response) {		
		Header connHeader = request.getHeaderLookupStruct().getHeader(KnownHeaderName.CONNECTION);
		
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
		createResponseAndSend(exc.getStatusCode(), "Something went wrong(are you hacking the system?)", "txt", "text/plain");
	}

}

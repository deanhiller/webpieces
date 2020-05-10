package org.webpieces.router.impl.proxyout;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.MissingPropException;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.BufferPool;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.exceptions.ControllerPageArgsException;
import org.webpieces.router.api.exceptions.WebSocketClosedException;
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
import com.webpieces.http2parser.api.dto.StatusCode;

//MUST NOT BE @Singleton!!! since this is created per request
public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	private final TemplateApi templatingService;
	private final StaticFileReader reader;
	private final ResponseCreator responseCreator;
	private final ChannelCloser channelCloser;
	private final BufferPool pool;
	
	private ProxyStreamHandle stream;
	//private HttpRequest request;
	private RouterRequest routerRequest;
	private Http2Request request;

	private FutureHelper futureUtil;

	@Inject
	public ProxyResponse(
		TemplateApi templatingService, 
		StaticFileReader reader,
		ResponseCreator responseCreator, 
		ChannelCloser channelCloser,
		BufferPool pool,
		FutureHelper futureUtil
	) {
		super();
		this.templatingService = templatingService;
		this.reader = reader;
		this.responseCreator = responseCreator;
		this.channelCloser = channelCloser;
		this.pool = pool;
		this.futureUtil = futureUtil;
	}

	public void init(RouterRequest req, ProxyStreamHandle responseSender) {
		this.routerRequest = req;
		this.request = req.orginalRequest;
		this.stream = responseSender;
	}

	@Override
	public CompletableFuture<Void> sendRedirect(RedirectResponse httpResponse) {
		if(log.isDebugEnabled())
			log.debug("Sending redirect response. req="+request);
		Http2Response response = responseCreator.createRedirect(request, httpResponse);
		
		log.info("sending REDIRECT response responseSender="+ stream);
		return stream.process(response).thenApply(w -> {
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
	public CompletableFuture<Void> sendRenderStatic(RenderStaticResponse renderStatic, ProxyStreamHandle handle) {
		if(log.isDebugEnabled())
			log.debug("Sending render static html response. req="+request);
		RequestInfo requestInfo = new RequestInfo(routerRequest, request, pool, stream);
		return futureUtil.catchBlockWrap(
			() -> reader.sendRenderStatic(requestInfo, renderStatic, handle), 
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
		return stream.maybeCompressAndSend(null, tuple, resp.getPayload()); 
	}
	
	public CompletableFuture<Void> createResponseAndSend(StatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");
		
		ResponseEncodingTuple tuple = responseCreator.createResponse(request, statusCode, extension, defaultMime, true);
		
		if(log.isDebugEnabled())
			log.debug("content about to be sent back="+content);
		
		Charset encoding = tuple.mimeType.htmlResponsePayloadEncoding;
		byte[] bytes = content.getBytes(encoding);
		
		return stream.maybeCompressAndSend(extension, tuple, bytes);
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


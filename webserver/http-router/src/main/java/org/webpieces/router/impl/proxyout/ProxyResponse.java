package org.webpieces.router.impl.proxyout;

import java.io.StringWriter;
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
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.proxyout.ResponseCreator.ResponseEncodingTuple;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2parser.api.dto.StatusCode;

//MUST NOT BE @Singleton!!! since this is created per request
public class ProxyResponse implements ResponseStreamer {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	private final TemplateApi templatingService;
	private final ResponseCreator responseCreator;
	
	private ProxyStreamHandle stream;
	private Http2Request request;

	private FutureHelper futureUtil;

	@Inject
	public ProxyResponse(
		TemplateApi templatingService, 
		ResponseCreator responseCreator, 
		BufferPool pool,
		FutureHelper futureUtil
	) {
		super();
		this.templatingService = templatingService;
		this.responseCreator = responseCreator;
		this.futureUtil = futureUtil;
	}

	public void init(RouterRequest req, ProxyStreamHandle responseSender) {
		this.request = req.originalRequest;
		this.stream = responseSender;
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
				 () -> stream.createResponseAndSend(statusCode, content, finalExt, "text/plain"), 
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

}


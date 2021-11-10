package org.webpieces.router.impl.proxyout;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.MissingPropException;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.RouterResponseHandler;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.controller.actions.HttpPort;
import org.webpieces.router.api.exceptions.ControllerPageArgsException;
import org.webpieces.router.api.exceptions.WebSocketClosedException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.router.impl.ReverseRoutes;
import org.webpieces.router.impl.UrlInfo;
import org.webpieces.router.impl.actions.PageArgListConverter;
import org.webpieces.router.impl.dto.RedirectResponse;
import org.webpieces.router.impl.dto.RenderContentResponse;
import org.webpieces.router.impl.dto.RenderResponse;
import org.webpieces.router.impl.dto.View;
import org.webpieces.router.impl.proxyout.ResponseCreator.ResponseEncodingTuple;
import org.webpieces.router.impl.routers.ExceptionWrap;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import org.webpieces.http.StatusCode;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class ProxyStreamHandle implements RouterStreamHandle {
	private static final Logger log = LoggerFactory.getLogger(ProxyStreamHandle.class);

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private TemplateApi templatingService;
	protected CompressionChunkingHandle handle;
	private ResponseCreator responseCreator;
	private FutureHelper futureUtil;
	private ReverseRoutes reverseRoutes;


	private Http2Request originalHttp2Request; //loaded on construction
	private MethodMeta methodMeta; //loaded just before invoking service

	@Inject
	public ProxyStreamHandle(
			TemplateApi templatingService,
			CompressionChunkingHandle handle,
			ResponseCreator responseCreator,
			FutureHelper futureUtil
	) {
		this.templatingService = templatingService;
		this.handle = handle;
		this.responseCreator = responseCreator;
		this.futureUtil = futureUtil;
	}

	//init methods done at different phases of the stack
	public void init(RouterResponseHandler originalHandle, Http2Request req) {
		this.originalHttp2Request = req;
		handle.init(originalHandle, req);
	}
	public void setRouterRequest(RouterRequest routerRequest) {
		handle.setRouterRequest(routerRequest);
	}
	public void initJustBeforeInvoke(ReverseRoutes reverseRoutes, MethodMeta invokeInfo) {
		this.reverseRoutes = reverseRoutes;
		this.methodMeta = invokeInfo;
	}

	public void turnCompressionOff() {
		handle.turnCompressionOff();
	}

	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		return handle.process(response);
	}

	@Override
	public Object getSocket() {
		return handle.getSocket();
	}

	@Override
	public Map<String, Object> getSession() {
		return handle.getSession();
	}

	@Override
	public boolean requestCameFromHttpsSocket() {
		return handle.requestCameFromHttpsSocket();
	}

	@Override
	public boolean requestCameFromBackendSocket() {
		return handle.requestCameFromBackendSocket();
	}

	@Deprecated
	@Override
	public Void closeIfNeeded() {
		return handle.closeIfNeeded();
	}

	@Override
	public PushStreamHandle openPushStream() {
		return handle.openPushStream();
	}

	@Override
	public XFuture<Void> cancel(CancelReason payload) {
		return handle.cancel(payload);
	}

	public boolean hasSentResponseAlready() {
		return handle.hasSentResponseAlready();
	}

	@Override
	public XFuture<Void> sendAjaxRedirect(RouteId id, Object ... args) {
		Map<String, Object> argMap = PageArgListConverter.createPageArgMap(args);
		return createRedirect(null, id, argMap, true);
	}

	@Override
	public XFuture<Void> sendPortRedirect(HttpPort port, RouteId id, Object ... args) {
		Map<String, Object> argMap = PageArgListConverter.createPageArgMap(args);
		return createRedirect(port, id, argMap, false);
	}

	@Override
	public XFuture<Void> sendFullRedirect(RouteId id, Object ... args) {
		Map<String, Object> argMap = PageArgListConverter.createPageArgMap(args);
		return createRedirect(null, id, argMap, false);
	}

	private XFuture<Void> createRedirect(HttpPort requestedPort, RouteId id, Map<String, Object> args, boolean isAjaxRedirect) {
		if(methodMeta == null) {
			throw new IllegalStateException("Somehow methodMeta is missing.  This method should only be called from filters and controllers");
		}
		RequestContext ctx = methodMeta.getCtx();

		RouterRequest request = ctx.getRequest();
		Method method = methodMeta.getLoadedController().getControllerMethod();

		UrlInfo urlInfo = reverseRoutes.routeToUrl(id, method, args, ctx, requestedPort);
		boolean isSecure = urlInfo.isSecure();
		int port = urlInfo.getPort();
		String path = urlInfo.getPath();

		RedirectResponse redirectResponse = new RedirectResponse(isAjaxRedirect, isSecure, request.domain, port, path);

		return sendRedirect(redirectResponse);
	}

	public XFuture<Void> sendRenderContent(RenderContentResponse resp) {
		Http2Request request = originalHttp2Request;
		ResponseEncodingTuple tuple = responseCreator.createContentResponse(request, resp.getStatusCode(), resp.getReason(), resp.getMimeType());
		return maybeCompressAndSend(request, null, tuple, resp.getPayload());
	}

	public XFuture<StreamWriter> sendRedirectAndClearCookie(RouterRequest req, String badCookieName) {
		RedirectResponse httpResponse = new RedirectResponse(false, req.isHttps, req.domain, req.port, req.relativePath);
		Http2Response response = responseCreator.createRedirect(originalHttp2Request, httpResponse);

		responseCreator.addDeleteCookie(response, badCookieName);

		log.info("sending REDIRECT(due to bad cookie) response responseSender="+ this);

		return process(response);
	}

	public XFuture<Void> sendRedirect(RedirectResponse httpResponse) {
		Http2Request request = originalHttp2Request;
		if(log.isDebugEnabled())
			log.debug("Sending redirect response. req="+request);
		Http2Response response = responseCreator.createRedirect(request, httpResponse);

		log.info("sending REDIRECT response responseSender="+ this);
		return process(response).thenApply(s -> null);
	}

	public XFuture<StreamWriter> topLevelFailure(Http2Request req, Throwable e) {
		if(ExceptionWrap.isChannelClosed(e))
			return XFuture.completedFuture(null);

		log.error("HUGE failure on incoming request="+req, e);
		
		if(log.isDebugEnabled())
			log.debug("Sending failure html response. req="+originalHttp2Request);

		//TODO: we should actually just render our own internalServerError.html page with styling and we could do that.

		//This is a final failure so we send a webpieces page next (in the future, we should just use a customer static html file if set)
		//An exception is caught here for one of two reasons that we know of
		//1. The app failed, and we called their internal error controller and that failed too!!!
		//2. There was a bug in webpieces

		String html =
				"<html>"
						+"   <head></head>"
						+ "  <body>"
						+ "      There was a bug in the developers application or webpieces server.  Contact website owner with a screen shot of this page."
						+ "      <br/><br/>"
						+ "      This page shows up for one of a few reasons.  In most cases, your app had a bug, then internal error had a bug too(you have two bugs to fix)"
						+ "      <ol>"
						+ "         <li>The app's error controller failed</li>"
						+ "         <li>IF you are running DevelopmentServer.java(hot recompile), you may have a bug in your Routers preventing loading of all plugins and controllers(compiler error, etc.)</li>"
						+ "         <li>Webpieces simply had a bug where it did not call the webapp developers internal error controller OR</li>"						
						+ "      </ol>"
						+ "  </body>"
						+ "</html>";

		//One of two cases at this point.  Either, we got far enough that we have a bunch of request info or we did not get far enough


		return createResponseAndSend(req, StatusCode.HTTP_500_INTERNAL_SERVER_ERROR, html, "html", "text/html").thenApply(voidd->null);
	}

	public XFuture<Void> createResponseAndSend(Http2Request request, StatusCode statusCode, String content, String extension, String defaultMime) {
		if(content == null)
			throw new IllegalArgumentException("content cannot be null");

		ResponseEncodingTuple tuple = responseCreator.createResponse(request, statusCode, extension, defaultMime, true);

		if(log.isDebugEnabled())
			log.debug("content about to be sent back="+content);

		Charset encoding = tuple.mimeType.htmlResponsePayloadEncoding;
		byte[] bytes = content.getBytes(encoding);

		return maybeCompressAndSend(request, extension, tuple, bytes);
	}

	public XFuture<Void> maybeCompressAndSend(Http2Request request, String extension, ResponseEncodingTuple tuple, byte[] bytes) {
		Http2Response resp = tuple.response;

		if(bytes.length == 0) {
			resp.setEndOfStream(true);
			return process(resp).thenApply(w -> null);
		}

		return sendChunkedResponse(request, resp, bytes);
	}

	private XFuture<Void> sendChunkedResponse(Http2Request req, Http2Response resp, byte[] bytes) {

		if(log.isDebugEnabled())
			log.debug("sending response. size="+bytes.length+" resp="+resp+" for req="+req+" responseSender="+ this);

		// Send the headers and get the responseid.
		return process(resp)
				.thenCompose(w -> createFrame(bytes, w));
	}

	private CompletionStage<Void> createFrame(byte[] bytes, StreamWriter writer) {
		DataFrame frame = new DataFrame();
		frame.setEndOfStream(true);
		frame.setData(dataGen.wrapByteArray(bytes));

		return writer.processPiece(frame);
	}

	public XFuture<Void> sendRenderHtml(RenderResponse resp) {
		Http2Request request = originalHttp2Request;
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
				statusCode = StatusCode.HTTP_404_NOT_FOUND;
				break;
			case INTERNAL_SERVER_ERROR:
				statusCode = StatusCode.HTTP_500_INTERNAL_SERVER_ERROR;
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
				() -> createResponseAndSend(request, statusCode, content, finalExt, "text/plain"),
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
	public Http2Response createBaseResponse(Http2Request req, String mimeType, int statusCode, String statusReason) {
		Http2Response response = responseCreator.addCommonHeaders2(req, mimeType, statusCode, statusReason);
		response.setEndOfStream(false); //This is for streaming so set eos=false;
		return response;
	}

	@Override
	public void closeSocket(String reason) {
		handle.closeSocket(reason);
	}
}

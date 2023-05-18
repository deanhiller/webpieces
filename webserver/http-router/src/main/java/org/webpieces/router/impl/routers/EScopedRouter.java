package org.webpieces.router.impl.routers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterHeader;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.router.api.exceptions.SpecificRouterInvokeException;
import org.webpieces.router.api.routebldr.AccessResult;
import org.webpieces.router.api.routebldr.ProcessCors;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.exceptions.WebpiecesException;
import org.webpieces.router.impl.RouterFutureUtil;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;

import com.webpieces.http2.api.streaming.StreamWriter;

public class EScopedRouter {
	private static final Logger log = LoggerFactory.getLogger(EScopedRouter.class);

	protected final RouterInfo routerInfo;
	private final Map<String, EScopedRouter> pathPrefixToNextRouter;
	private List<AbstractRouter> routers;
	private RouterFutureUtil futureUtil;

	public EScopedRouter(
			RouterFutureUtil futureUtil,
			RouterInfo routerInfo, 
			Map<String, EScopedRouter> pathPrefixToNextRouter,
			List<AbstractRouter> routers
	) {
		this.futureUtil = futureUtil;
		this.routerInfo = routerInfo;
		this.pathPrefixToNextRouter = pathPrefixToNextRouter;
		this.routers = routers;
	}

	public RouterStreamRef invokeRoute(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		//ANY failures shortcircuit cancellation
		try {
			return invokeRouteImpl(ctx, handler, subPath);
		} catch(Throwable t) {
			return new RouterStreamRef("failedRoute", t);
		}
	}
	
	public RouterStreamRef invokeRouteImpl(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		if("".equals(subPath))
			return findAndInvokeRoute(ctx, handler, subPath);
		else if(!subPath.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");
		
		String prefix = subPath;
		int index = subPath.indexOf("/", 1);
		if(index == 1) {
			XFuture<StreamWriter> future = new XFuture<>();
			future.completeExceptionally(new NotFoundException("Bad path="+ctx.getRequest().relativePath+" request="+ctx.getRequest()));
			return new RouterStreamRef("badPath", future, null);
		} else if(index > 1) {
			prefix = subPath.substring(0, index);
		}

		EScopedRouter routeInfo = getPathPrefixToNextRouter().get(prefix);
		if(routeInfo != null) {
			if(index < 0)
				return routeInfo.invokeRoute(ctx, handler, "");
			
			String newRelativePath = subPath.substring(index, subPath.length());
			return routeInfo.invokeRoute(ctx, handler, newRelativePath);
		}
		
		return findAndInvokeRoute(ctx, handler, subPath);
	}
	
	private RouterStreamRef findAndInvokeRoute(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		RouterRequest routerReq = ctx.getRequest();
		if(routerReq.method == HttpMethod.OPTIONS) {
			return respondToOptionsRequest(ctx, handler, subPath);
		}

		String forwarded = routerReq.getSingleHeaderValue(Http2HeaderName.X_FORWARDED_PROTO.getHeaderName());
		String scheme = routerReq.getSingleHeaderValueOrDefault(Http2HeaderName.SCHEME.getHeaderName(), "");
		if (!Strings.isNullOrEmpty(forwarded)) scheme = "https";

		String authority = routerReq.getSingleHeaderValueOrDefault(Http2HeaderName.AUTHORITY.getHeaderName(), "");
		String fullDomain = scheme+"://"+authority;

		String origin = routerReq.getSingleHeaderValueOrDefault(Http2HeaderName.ORIGIN.getHeaderName(), "");
		boolean isCorsRequest = !Strings.isNullOrEmpty(origin) && !fullDomain.equals(origin);

		return findAndInvokeImpl(ctx, handler, subPath, isCorsRequest);
	}

	private RouterStreamRef findAndInvokeImpl(RequestContext ctx, ProxyStreamHandle handler, String subPath, boolean isCorsRequest) {
		for(AbstractRouter router : routers) {
			MatchResult2 result = router.matches(ctx.getRequest(), subPath);

			if(result.isMatches()) {
				if(log.isTraceEnabled()) {
					logDetail("FOUND MATCH.", ctx, router);
				}

				ctx.setPathParams(result.getPathParams());
				return invokeRouter(router, ctx, handler, isCorsRequest);
			} else if(log.isTraceEnabled()) {
				logDetail("DOES NOT MATCH.", ctx, router);
			}
		}

		XFuture<StreamWriter> failedFuture = futureUtil.failedFuture(new NotFoundException("route not found"));
		return new RouterStreamRef("notFoundEScope", failedFuture, null);
	}

	private void logDetail(String prefix, RequestContext ctx, AbstractRouter router) {
		MatchInfo matchInfo = router.getMatchInfo();
		RouterRequest req = ctx.getRequest();
		log.trace(prefix+"   matchInfo=\n"+matchInfo+"\n\nRouterReq="+req);
	}

	private RouterStreamRef respondToOptionsRequest(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		List<FContentRouter> matchingMethods = new ArrayList<>();
		for(AbstractRouter router : routers) {
			if(!(router instanceof FContentRouter))
				continue;

			FContentRouter contentRouter = (FContentRouter) router;
			if(router.getMatchInfo().acceptsProtocol(ctx.getRequest().isHttps)
				&& router.getMatchInfo().patternMatches(subPath)
					&& contentRouter.getCorsProcessor() != null
			) {
				matchingMethods.add(contentRouter);
			}
		}

		if(matchingMethods.size() == 0) {
			send403Response(handler, "No methods on this url path allow CORS requests");
		} else {
			doCorsProessing(ctx, handler, matchingMethods);
		}

		XFuture<StreamWriter> empty = XFuture.completedFuture(new EmptyWriter());
		return new RouterStreamRef("optionsCorsEmptyWriter", empty, null);
	}

	private void send403Response(ProxyStreamHandle handler, String reason) {
		Http2Response response = new Http2Response();
		response.addHeader(new Http2Header(Http2HeaderName.STATUS, "403"));
		response.addHeader(new Http2Header("Webpieces-Reason", reason));

		XFuture<StreamWriter> process = handler.process(response);

		try {
			process.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw SneakyThrow.sneak(e);
		}
	}

	private void doCorsProessing(RequestContext ctx, ProxyStreamHandle handler, List<FContentRouter> matchingMethods) {
		ProcessCors corsProcessor = matchingMethods.get(0).getCorsProcessor();
		List<HttpMethod> methodsSupported = new ArrayList<>();
		for (FContentRouter r : matchingMethods) {
			HttpMethod httpMethod = r.matchInfo.getHttpMethod();
			methodsSupported.add(httpMethod);
			if (corsProcessor != r.getCorsProcessor()) {
				throw new IllegalStateException("Developer has a mistake in routes file adding different ProcessCors to different methods of same url and port types");
			}
		}

		try {
			corsProcessor.processOptionsCors(ctx.getRequest().originalRequest, methodsSupported, handler);
		} catch (Throwable e) {
			log.error("Customer code for class=" + corsProcessor + " failed", e);
		}
	}

	private RouterStreamRef invokeRouter(AbstractRouter router, RequestContext ctx,
												 ProxyStreamHandle handler, boolean isCorsRequest) {
		RouterStreamRef streamRef = invokeWithProtection(router, ctx, handler, isCorsRequest);
		
		XFuture<StreamWriter> writer =
				streamRef.getWriter()
					.handle( (r, t) -> {
						if(t == null)
							return XFuture.completedFuture(r);
			
						XFuture<StreamWriter> fut = new XFuture<>();
						Throwable exc = convert(router.getMatchInfo(), t);
						fut.completeExceptionally(exc);
						return fut;
					}).thenCompose(Function.identity());
		
		return new RouterStreamRef("eScoped2", writer, streamRef);
	}
	
	private RouterStreamRef invokeWithProtection(AbstractRouter router, RequestContext ctx,
			 ProxyStreamHandle handler, boolean isCorsRequest) {
		try {

			if(isCorsRequest && isFailSecurityCheck(router, ctx, handler)) {
					return new RouterStreamRef("failCors");
			}

			return router.invoke(ctx, handler);
		} catch(Throwable e) {
			//convert to async exception
			return new RouterStreamRef("EScopedRouter", e);
		}
	}

	private boolean isFailSecurityCheck(AbstractRouter router, RequestContext ctx, ProxyStreamHandle handler) {
		if(!(router instanceof FContentRouter)) {
			//we only do CORS for content requests(json/xml/etc)
			send403Response(handler, "Only content routes allow CORS requests. Router not supported="+router.getClass());
			return true;
		}

		FContentRouter contentRouter = (FContentRouter) router;
		ProcessCors corsProcessor = contentRouter.getCorsProcessor();
		if(corsProcessor == null) {
			send403Response(handler, "This method and path did not support CORS");
			return true;
		}

		AccessResult accessResult = corsProcessor.isAccessAllowed(ctx);
		if(!accessResult.isAllowed()) {
			send403Response(handler, accessResult.getReasonForDenial());
			return true;
		}

		return false;
	}

	private Throwable convert(MatchInfo info, Throwable t) {
		if(t instanceof WebpiecesException)
			return t;
		return new SpecificRouterInvokeException(info, t);
	}

	public Map<String, EScopedRouter> getPathPrefixToNextRouter() {
		return pathPrefixToNextRouter;
	}

	@Override
	public String toString() {
		return build("");
	}

	public void printRoutes(boolean isHttps, String tabSpaces) {
		//This is a pain but dynamically build up the html
		String routeHtml = build(tabSpaces);
		
		//print in warn so it's in red for anyone and to stderr IF they have debug enabled
		//it's kind of weird BUT great for tests
		if(!isHttps)
			log.warn("WARNING: The request is NOT https so perhaps your route is only accessible over https so modify your request" + routeHtml);
		else
			log.warn(routeHtml);
	}

	public String build(String spacing) {
		String text = "\n";
		
		for(Map.Entry<String, EScopedRouter> entry : pathPrefixToNextRouter.entrySet()) {
			EScopedRouter childRouting = entry.getValue();
			text += spacing+ "SCOPE:"+entry.getKey();
			text += childRouting.build(spacing + spacing);
		}
		
		for(AbstractRouter route: routers) {
			text += spacing+route.getMatchInfo().getLoggableString(" ")+"\n";
		}
		
		text+="\n";
		
		return text;
	}

	public String buildHtml(boolean isHttps, HttpMethod method, String path, String spacing) {
		String scope = null;
		String theRest = null;
		
		if(path != null) {
			int indexOf = path.indexOf("/",1);
			if(indexOf > 0) {
				scope = path.substring(0, indexOf);
				theRest = path.substring(indexOf);
			}
		}
		
		String html = "";
		
		boolean foundScope = false;
		for(Map.Entry<String, EScopedRouter> entry : pathPrefixToNextRouter.entrySet()) {
			EScopedRouter childRouting = entry.getValue();
			
			if(entry.getKey().equals(scope)) {
				foundScope = true;
				//add this special one to the beginning since it's a match...
				String newSection = spacing+"<li style=\"color:green;\">SCOPE:"+entry.getKey()+"</li>\n";
				newSection += spacing+childRouting.buildHtml(isHttps, method, theRest, spacing+spacing);
				html = newSection + html;
			} else {
				//add to end as it doesn't match
				html += spacing+"<li>SCOPE:"+entry.getKey()+"</li>\n";
				html += spacing+childRouting.buildHtml(isHttps, method, null, spacing+spacing);
			}
		}
		
		String leftOvers = "";
		for(AbstractRouter route: routers) {
			boolean pathMatches = false;
			boolean portMatches = false;
			boolean methodMatches = false;
			
			if(!foundScope && path != null) {
				pathMatches = route.getMatchInfo().patternMatches(path);
				methodMatches = route.getMatchInfo().methodMatches(method);
				portMatches = route.getMatchInfo().acceptsProtocol(isHttps);
			}
			
			leftOvers += spacing+"<li>"+route.getMatchInfo().getLoggableHtml(portMatches, methodMatches, pathMatches, "&nbsp;")+"</li>\n";
		}
		
		if(foundScope) {
			html = html+leftOvers;
		} else {
			html = leftOvers+html;
		}
		
		html= "<ul>\n"+html+"</ul>\n";
		
		return html;		
	}



}

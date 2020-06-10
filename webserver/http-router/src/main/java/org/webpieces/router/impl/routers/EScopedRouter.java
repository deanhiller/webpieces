package org.webpieces.router.impl.routers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.exceptions.SpecificRouterInvokeException;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.RouterFutureUtil;
import org.webpieces.router.impl.dto.RouteType;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.router.impl.routeinvoker.RouterStreamRef;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2.api.streaming.MyStreamRef;
import com.webpieces.http2.api.streaming.StreamRef;
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
			CompletableFuture<StreamWriter> future = new CompletableFuture<>();
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
		for(AbstractRouter router : routers) {
			MatchResult2 result = router.matches(ctx.getRequest(), subPath);
			if(result.isMatches()) {
				ctx.setPathParams(result.getPathParams());
				
				return invokeRouter(router, ctx, handler);
			}
		}

		CompletableFuture<StreamWriter> failedFuture = futureUtil.failedFuture(new NotFoundException("route not found"));
		return new RouterStreamRef("notFoundEScope", failedFuture, null);
	}
	
	private RouterStreamRef invokeRouter(AbstractRouter router, RequestContext ctx,
												 ProxyStreamHandle handler) {
		RouterStreamRef streamRef = invokeWithProtection(router, ctx, handler);
		
		CompletableFuture<StreamWriter> writer = 
				streamRef.getWriter()
					.handle( (r, t) -> {
						if(t == null)
							return CompletableFuture.completedFuture(r);
			
						CompletableFuture<StreamWriter> fut = new CompletableFuture<>();
						Throwable exc = convert(router.getMatchInfo(), t);
						fut.completeExceptionally(exc);
						return fut;
					}).thenCompose(Function.identity());
		
		return new RouterStreamRef("eScoped2", writer, streamRef);
	}
	
	private RouterStreamRef invokeWithProtection(AbstractRouter router, RequestContext ctx,
			 ProxyStreamHandle handler) {
		try {
			return router.invoke(ctx, handler);
		} catch(Throwable e) {
			//convert to async exception
			return new RouterStreamRef("EScopedRouter", e);
		}
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
				Pattern pattern = route.getMatchInfo().getPattern();
				Matcher matcher = pattern.matcher(path);
				pathMatches = matcher.matches();
				methodMatches = method == route.getMatchInfo().getHttpMethod();

				if(route.getMatchInfo().getExposedPorts() == Port.BOTH)
					portMatches = true;
				else if(isHttps) //ok, it's not BOTH, so if isHttps, then it matches Port.HTTPS
					portMatches = true;
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

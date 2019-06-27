package org.webpieces.router.impl.routers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.exceptions.SpecificRouterInvokeException;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.util.filters.ExceptionUtil;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class DScopedRouter {
	private static final Logger log = LoggerFactory.getLogger(DScopedRouter.class);

	protected final RouterInfo routerInfo;
	private final Map<String, DScopedRouter> pathPrefixToNextRouter;
	private List<AbstractRouter> routers;

	public DScopedRouter(RouterInfo routerInfo, Map<String, DScopedRouter> pathPrefixToNextRouter, List<AbstractRouter> routers) {
		this.routerInfo = routerInfo;
		this.pathPrefixToNextRouter = pathPrefixToNextRouter;
		this.routers = routers;
	}
	
	public CompletableFuture<Void> invokeRoute(RequestContext ctx, ResponseStreamer responseCb, String subPath) {
		if("".equals(subPath))
			return findAndInvokeRoute(ctx, responseCb, subPath);
		else if(!subPath.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");
		
		String prefix = subPath;
		int index = subPath.indexOf("/", 1);
		if(index == 1) {
			CompletableFuture<Void> future = new CompletableFuture<>();
			future.completeExceptionally(new NotFoundException("Bad path="+ctx.getRequest().relativePath+" request="+ctx.getRequest()));
			return future;
		} else if(index > 1) {
			prefix = subPath.substring(0, index);
		}

		DScopedRouter routeInfo = getPathPrefixToNextRouter().get(prefix);
		if(routeInfo != null) {
			if(index < 0)
				return routeInfo.invokeRoute(ctx, responseCb, "");
			
			String newRelativePath = subPath.substring(index, subPath.length());
			return routeInfo.invokeRoute(ctx, responseCb, newRelativePath);
		}
		
		return findAndInvokeRoute(ctx, responseCb, subPath);
	}
	
	private CompletableFuture<Void> findAndInvokeRoute(RequestContext ctx, ResponseStreamer responseCb, String subPath) {
		for(AbstractRouter router : routers) {
			MatchResult2 result = router.matches(ctx.getRequest(), subPath);
			if(result.isMatches()) {
				ctx.setPathParams(result.getPathParams());
				
				return invokeRouter(router, ctx, responseCb);
			}
		}

		return ExceptionUtil.<Void>failedFuture(new NotFoundException("route not found"));
	}
	
	private CompletableFuture<Void> invokeRouter(AbstractRouter router, RequestContext ctx,
			ResponseStreamer responseCb) {
		//We re-use this method to avoid chaining when it's a NotFoundException
		return ExceptionUtil.<Void>wrapException(
			() -> router.invoke(ctx, responseCb),
			(t) -> convert(router.getMatchInfo(), t)
		);
	}

	private Throwable convert(MatchInfo info, Throwable t) {
		if(t instanceof WebpiecesException)
			return t;
		return new SpecificRouterInvokeException(info, t);
	}

	public Map<String, DScopedRouter> getPathPrefixToNextRouter() {
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
		
		for(Map.Entry<String, DScopedRouter> entry : pathPrefixToNextRouter.entrySet()) {
			DScopedRouter childRouting = entry.getValue();
			text += spacing+ "SCOPE:"+entry.getKey();
			text += childRouting.build(spacing + spacing);
		}
		
		for(AbstractRouter route: routers) {
			text += spacing+route.getMatchInfo().getLoggableString(" ")+"\n";
		}
		
		text+="\n";
		
		return text;
	}

	public String buildHtml(String spacing) {
		
		String html = "<ul>\n";

		for(AbstractRouter route: routers) {
			html += spacing+"<li>"+route.getMatchInfo().getLoggableString("&nbsp;")+"</li>\n";
		}
		
		for(Map.Entry<String, DScopedRouter> entry : pathPrefixToNextRouter.entrySet()) {
			DScopedRouter childRouting = entry.getValue();
			html += spacing+"<li>SCOPE:"+entry.getKey()+"</li>\n";
			html += spacing+childRouting.buildHtml(spacing+spacing);
		}
		
		html+="</ul>\n";
		
		return html;		
	}



}

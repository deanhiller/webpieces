package org.webpieces.router.impl.routers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.exceptions.SpecificRouterInvokeException;
import org.webpieces.router.api.exceptions.WebpiecesException;
import org.webpieces.router.impl.model.MatchResult2;
import org.webpieces.router.impl.model.RouterInfo;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;
import org.webpieces.util.futures.FutureHelper;

import com.webpieces.http2engine.api.StreamWriter;

public class EScopedRouter {
	private static final Logger log = LoggerFactory.getLogger(EScopedRouter.class);

	protected final RouterInfo routerInfo;
	private final Map<String, EScopedRouter> pathPrefixToNextRouter;
	private List<AbstractRouter> routers;
	private FutureHelper futureUtil;

	public EScopedRouter(FutureHelper futureUtil, RouterInfo routerInfo, Map<String, EScopedRouter> pathPrefixToNextRouter, List<AbstractRouter> routers) {
		this.futureUtil = futureUtil;
		this.routerInfo = routerInfo;
		this.pathPrefixToNextRouter = pathPrefixToNextRouter;
		this.routers = routers;
	}
	
	public CompletableFuture<StreamWriter> invokeRoute(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		if("".equals(subPath))
			return findAndInvokeRoute(ctx, handler, subPath);
		else if(!subPath.startsWith("/"))
			throw new IllegalArgumentException("path must start with /");
		
		String prefix = subPath;
		int index = subPath.indexOf("/", 1);
		if(index == 1) {
			CompletableFuture<StreamWriter> future = new CompletableFuture<>();
			future.completeExceptionally(new NotFoundException("Bad path="+ctx.getRequest().relativePath+" request="+ctx.getRequest()));
			return future;
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
	
	private CompletableFuture<StreamWriter> findAndInvokeRoute(RequestContext ctx, ProxyStreamHandle handler, String subPath) {
		for(AbstractRouter router : routers) {
			MatchResult2 result = router.matches(ctx.getRequest(), subPath);
			if(result.isMatches()) {
				ctx.setPathParams(result.getPathParams());
				
				return invokeRouter(router, ctx, handler);
			}
		}

		return futureUtil.<StreamWriter>failedFuture(new NotFoundException("route not found"));
	}
	
	private CompletableFuture<StreamWriter> invokeRouter(AbstractRouter router, RequestContext ctx,
												 ProxyStreamHandle handler) {
		//We re-use this method to avoid chaining when it's a NotFoundException
		return futureUtil.catchBlockWrap(
			() -> router.invoke(ctx, handler),
			(t) -> convert(router.getMatchInfo(), t)
		);
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

	public String buildHtml(String path, String spacing) {
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
		
		for(Map.Entry<String, EScopedRouter> entry : pathPrefixToNextRouter.entrySet()) {
			EScopedRouter childRouting = entry.getValue();
			
			if(entry.getKey().equals(scope)) {
				//add this special one to the beginning since it's a match...
				String newSection = spacing+"<li style=\"color:red;\">SCOPE:"+entry.getKey()+"</li>\n";
				newSection += spacing+childRouting.buildHtml(theRest, spacing+spacing);
				html = newSection + html;
			} else {
				//add to end as it doesn't match
				html += spacing+"<li>SCOPE:"+entry.getKey()+"</li>\n";
				html += spacing+childRouting.buildHtml(theRest, spacing+spacing);
			}
		}
		
		for(AbstractRouter route: routers) {
			html += spacing+"<li>"+route.getMatchInfo().getLoggableString("&nbsp;")+"</li>\n";
		}
		
		html= "<ul>\n"+html+"</ul>\n";
		
		return html;		
	}



}

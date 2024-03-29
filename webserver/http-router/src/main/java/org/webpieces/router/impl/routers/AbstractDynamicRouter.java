package org.webpieces.router.impl.routers;

import java.util.regex.Matcher;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.api.streams.StreamService;
import org.webpieces.router.impl.dto.RouteType;

public abstract class AbstractDynamicRouter extends AbstractRouterImpl {

	protected StreamService dynamicInfo;
	
	public AbstractDynamicRouter(MatchInfo matchInfo) {
		super(matchInfo);
	}
	
	@Override
	protected Matcher matchesAndParseParams(RouterRequest request, String path) {
		if(!matchInfo.acceptsProtocol(request.isHttps)) {
			return null; //route is https but request is http so not allowed
		} else if(!matchInfo.methodMatches(request.method)) {
			return null;
		}
		
		Matcher matcher = matchInfo.getPattern().matcher(path);
		return matcher;
	}
	
	public void setDynamicInfo(StreamService dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}
	
	public abstract RouteType getRouteType();
}

package org.webpieces.router.impl.routers;

import java.util.regex.Matcher;

import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.router.api.routes.Port;
import org.webpieces.router.impl.routebldr.BaseRouteInfo;

public abstract class AbstractDynamicRouter extends AbstractRouterImpl {

	protected DynamicInfo dynamicInfo;
	//hmmmm, this was a bit of a pain.  It is only set once but it's hard to design the code to pass in during construction
	//without quite a bit of work
	protected BaseRouteInfo baseRouteInfo;
	
	public AbstractDynamicRouter(MatchInfo matchInfo) {
		super(matchInfo);
	}
	
	@Override
	protected Matcher matchesAndParseParams(RouterRequest request, String path) {
		if(matchInfo.getExposedPorts() == Port.HTTPS && !request.isHttps) {
			//NOTE: we cannot do if isHttpsRoute != request.isHttps as every http route is 
			//allowed over https as well by default.  so 
			//isHttpsRoute=false and request.isHttps=true is allowed
			//isHttpsRoute=false and request.isHttps=false is allowed
			//isHttpsRoute=true  and request.isHttps=true is allowed
			return null; //route is https but request is http so not allowed
		} else if(matchInfo.getHttpMethod() != request.method) {
			return null;
		}
		
		Matcher matcher = matchInfo.getPattern().matcher(path);
		return matcher;
	}
	
	public void setBaseRouteInfo(BaseRouteInfo baseRouteInfo) {
		this.baseRouteInfo = baseRouteInfo;
	}
	
	public void setDynamicInfo(DynamicInfo dynamicInfo) {
		this.dynamicInfo = dynamicInfo;
	}
}

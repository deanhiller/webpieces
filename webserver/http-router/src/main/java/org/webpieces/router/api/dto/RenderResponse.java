package org.webpieces.router.api.dto;

import java.util.Map;

import org.webpieces.router.api.actions.RenderHtml;

public class RenderResponse implements RenderHtml {

	private View view;
	private RouteType routeType;
	private Map<String, Object> pageArgs;

	public RenderResponse(View view2, Map<String, Object> pageArgs, RouteType routeType) {
		this.view = view2;
		this.pageArgs = pageArgs;
		this.routeType = routeType;
	}

	public View getView() {
		return view;
	}

	public RouteType getRouteType() {
		return routeType;
	}

	public Map<String, Object> getPageArgs() {
		return pageArgs;
	}
	
	
}

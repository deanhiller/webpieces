package org.webpieces.router.api.dto;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.actions.RenderHtml;

public class RenderResponse implements RenderHtml {

	public View view;
	public RouteType routeType;
	public Map<String, Object> pageArgs;
	public List<Cookie> cookies;

	public RenderResponse(View view2, Map<String, Object> pageArgs, RouteType routeType, List<Cookie> cookies) {
		this.view = view2;
		this.pageArgs = pageArgs;
		this.routeType = routeType;
		this.cookies = cookies;
	}

}

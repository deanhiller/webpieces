package org.webpieces.router.api.dto;

import java.util.Map;

public class RenderResponse {

	private View view;
	private boolean isNotFoundRoute;
	private Map<String, Object> pageArgs;

	public RenderResponse(View view2, Map<String, Object> pageArgs, boolean isNotFoundRoute) {
		this.view = view2;
		this.pageArgs = pageArgs;
		this.isNotFoundRoute = isNotFoundRoute;
	}

	public View getView() {
		return view;
	}

	public boolean isNotFoundRoute() {
		return isNotFoundRoute;
	}

	public Map<String, Object> getPageArgs() {
		return pageArgs;
	}
	
	
}

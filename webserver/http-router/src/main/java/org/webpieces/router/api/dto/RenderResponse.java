package org.webpieces.router.api.dto;

public class RenderResponse {

	private View view;
	private boolean isNotFoundRoute;

	public RenderResponse(View view, boolean isNotFoundRoute) {
		this.view = view;
		this.isNotFoundRoute = isNotFoundRoute;
	}

	public View getView() {
		return view;
	}

	public boolean isNotFoundRoute() {
		return isNotFoundRoute;
	}
	
}

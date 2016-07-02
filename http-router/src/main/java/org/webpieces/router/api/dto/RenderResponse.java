package org.webpieces.router.api.dto;

public class RenderResponse {

	private View view;

	public RenderResponse(View view) {
		this.view = view;
	}

	public View getView() {
		return view;
	}

}

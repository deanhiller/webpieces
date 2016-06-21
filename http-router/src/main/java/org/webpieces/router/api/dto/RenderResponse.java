package org.webpieces.router.api.dto;

public class RenderResponse {

	public String view;
	public Object[] args;

	public RenderResponse(String view, Object ... args) {
		this.view = view;
		this.args = args;
	}

}

package org.webpieces.router.api.actions;

public class RenderHtml implements Action {

	private String view;
	private Object[] pageArgs;

	protected RenderHtml(String view, Object ... pageArgs) {
		this.view = view;
		this.pageArgs = pageArgs;
	}
	
	protected RenderHtml(Object ... pageArgs) {
		this.pageArgs = pageArgs;
	}

	
	public String getView() {
		return view;
	}

	public Object[] getPageArgs() {
		return pageArgs;
	}
	
}

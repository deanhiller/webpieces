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

	
	public static RenderHtml createView(String view, Object ... pageArgs) {
		return new RenderHtml(view, pageArgs);
	}
	
	public static RenderHtml create(Object ... pageArgs) {
		return new RenderHtml(pageArgs);
	}

	public String getView() {
		return view;
	}

	public Object[] getPageArgs() {
		return pageArgs;
	}
	
}

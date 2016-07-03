package org.webpieces.router.api.actions;

import org.webpieces.router.api.dto.View;

public class RenderHtml implements Action {

	private Object[] pageArgs;
	private View view;

//	protected RenderHtml(String view, Object ... pageArgs) {
//		this.view = view;
//		this.pageArgs = pageArgs;
//	}
	
	protected RenderHtml(Object ... pageArgs) {
		this.pageArgs = pageArgs;
	}

	
	public View getView() {
		return view;
	}

	public Object[] getPageArgs() {
		return pageArgs;
	}

}

package org.webpieces.router.api.controller.actions;

import org.webpieces.ctx.api.RequestContext;

public class FlashAndRedirect {

	private RequestContext context;
	private String globalMessage;
	private String[] secureFields = new String[0];
	private String idField;
	private Object idValue;
	private String[] pageArgs = new String[0];

	public FlashAndRedirect(RequestContext context, String globalMessage) {
		this.context = context;
		this.globalMessage = globalMessage;
	}

	public void setSecureFields(String ... secureFields) {
		this.secureFields = secureFields;
	}

	public void setIdFieldAndValue(String idField, Object idValue) {
		this.idField = idField;
		this.idValue = idValue;
	}

	public RequestContext getContext() {
		return context;
	}

	public String getGlobalMessage() {
		return globalMessage;
	}

	public String[] getSecureFields() {
		return secureFields;
	}

	public String getIdField() {
		return idField;
	}

	public Object getIdValue() {
		return idValue;
	}

	public void setPageAndRouteArguments(String ... pageArgs) {
		this.pageArgs = pageArgs;
	}

	public String[] getPageArgs() {
		return pageArgs;
	}
	
}

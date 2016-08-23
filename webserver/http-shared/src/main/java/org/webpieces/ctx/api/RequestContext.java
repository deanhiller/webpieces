package org.webpieces.ctx.api;

import java.util.Map;
import java.util.Set;

public class RequestContext {

	private Validation validation;
	private Flash flash;
	private Session session;
	private RouterRequest request;
	private Messages messages;

	public RequestContext(Validation validation, Flash flash, Session session, RouterRequest request, Messages messages) {
		this.validation = validation;
		this.flash = flash;
		this.request = request;
		this.session = session;
		this.messages = messages;
	}
	
	public RouterRequest getRequest() {
		return request;
	}

	public Flash getFlash() {
		return flash;
	}

	public Validation getValidation() {
		return validation;
	}

	public Session getSession() {
		return session;
	}

	public Messages getMessages() {
		return messages;
	}
	
	/**
	 * 
	 * @param secureFieldNames fieldNames that are secure and should NOT be transferred to flash
	 */
	public void moveFormParamsToFlash(Set<String> secureFieldNames) {
		Map<String, String> fields = request.multiPartFields;
		flash.saveFormParams(fields, secureFieldNames);
	}
	
}

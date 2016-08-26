package org.webpieces.ctx.api;

import java.util.Map;
import java.util.Set;

public class RequestContext {

	private Validation validation;
	private FlashSub flash;
	private Session session;
	private RouterRequest request;
	private Messages messages;

	public RequestContext(Validation validation, FlashSub flash, Session session, RouterRequest request) {
		this.validation = validation;
		this.flash = flash;
		this.request = request;
		this.session = session;
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

	public void setMessages(Messages messages) {
		this.messages = messages;
	}
	
}

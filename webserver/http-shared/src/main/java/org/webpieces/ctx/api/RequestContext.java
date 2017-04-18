package org.webpieces.ctx.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestContext {

	public static final String SECURE_TOKEN_FORM_NAME = "__secureToken";

	//incoming router request
	private RouterRequest request;
	//the params parsed from the request url if any
	private Map<String, String> pathParams;

	private Validation validation;
	private FlashSub flash;
	private Session session;
	private Messages messages;
	private List<OverwritePlatformResponse> callbacks = new ArrayList<>();

	public RequestContext(Validation validation, FlashSub flash, Session session, RouterRequest request, Map<String, String> pathParams) {
		this.request = request;
		this.pathParams = pathParams;

		this.validation = validation;
		this.flash = flash;
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
	
	public Map<String, String> getPathParams() {
		return pathParams;
	}

	/**
	 * 
	 * @param secureFieldNames fieldNames that are secure and should NOT be transferred to flash
	 */
	public void moveFormParamsToFlash(Set<String> secureFieldNames) {
		Map<String, List<String>> fields = request.multiPartFields;
		flash.saveFormParams(fields, secureFieldNames);
	}

	public void setMessages(Messages messages) {
		this.messages = messages;
	}
	
	public void addModifyResponse(OverwritePlatformResponse callback) {
		this.callbacks.add(callback);
	}

	public List<OverwritePlatformResponse> getCallbacks() {
		return callbacks;
	}

}

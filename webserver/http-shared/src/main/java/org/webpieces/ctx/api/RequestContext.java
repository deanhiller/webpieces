package org.webpieces.ctx.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestContext {

	private Validation validation;
	private Flash flash;
	private Session session;
	private RouterRequest request;

	public RequestContext(Validation validation, Flash flash, Session session, RouterRequest request) {
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

	/**
	 * 
	 * @param secureFieldNames fieldNames that are secure and should NOT be transferred to flash
	 */
	public void moveFormParamsToFlash(Set<String> secureFieldNames) {
		Map<String, List<String>> fields = request.multiPartFields;
		flash.saveFormParams(fields, secureFieldNames);
	}
	
}

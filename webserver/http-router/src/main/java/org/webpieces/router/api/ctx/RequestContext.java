package org.webpieces.router.api.ctx;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.dto.RouterRequest;

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

	public void moveFormParamsToFlash() {
		Map<String, List<String>> fields = request.multiPartFields;
		flash.saveFormParams(fields);
	}
	
}

package org.webpieces.templating.impl;

import java.util.concurrent.CompletionException;

public class TemplateException extends CompletionException {

	private static final long serialVersionUID = 1L;
	private String subMessage;

	public TemplateException(String message, String subMessage, Throwable t) {
		super(message, t);
		this.subMessage = subMessage;
	}

	public String getSubMessage() {
		return subMessage;
	}

}

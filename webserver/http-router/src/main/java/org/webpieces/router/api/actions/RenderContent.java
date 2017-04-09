package org.webpieces.router.api.actions;

import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class RenderContent implements Action {

	private byte[] content;
	private int statusCode;
	private MimeTypeResult mimeType;

	public RenderContent(byte[] payload, int statusCode, MimeTypeResult mimeType) {
		this.content = payload;
		this.statusCode = statusCode;
		this.mimeType = mimeType;
	}
	
	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] payload) {
		this.content = payload;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public MimeTypeResult getMimeType() {
		return mimeType;
	}
	
}

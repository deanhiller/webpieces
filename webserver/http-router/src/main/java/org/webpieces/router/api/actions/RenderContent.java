package org.webpieces.router.api.actions;

import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class RenderContent implements Action {

	private byte[] content;
	private KnownStatusCode statusCode;
	private MimeTypeResult mimeType;

	public RenderContent(byte[] payload, KnownStatusCode statusCode, MimeTypeResult mimeType) {
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

	public KnownStatusCode getStatusCode() {
		return statusCode;
	}

	public MimeTypeResult getMimeType() {
		return mimeType;
	}
	
}

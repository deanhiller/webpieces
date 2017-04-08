package org.webpieces.router.api.dto;

import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class RenderContentResponse {

	private byte[] payload;
	private KnownStatusCode statusCode;
	private MimeTypeResult mimeType;

	public RenderContentResponse(byte[] payload, KnownStatusCode statusCode, MimeTypeResult mimeType) {
		this.payload = payload;
		this.statusCode = statusCode;
		this.mimeType = mimeType;
	}
	
	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public KnownStatusCode getStatusCode() {
		return statusCode;
	}

	public MimeTypeResult getMimeType() {
		return mimeType;
	}
	
}

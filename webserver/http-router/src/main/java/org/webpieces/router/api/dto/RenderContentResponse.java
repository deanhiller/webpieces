package org.webpieces.router.api.dto;

import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class RenderContentResponse {

	private byte[] payload;
	private int statusCode;
	private MimeTypeResult mimeType;

	public RenderContentResponse(byte[] payload, int statusCode, MimeTypeResult mimeType) {
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

	public int getStatusCode() {
		return statusCode;
	}

	public MimeTypeResult getMimeType() {
		return mimeType;
	}
	
}

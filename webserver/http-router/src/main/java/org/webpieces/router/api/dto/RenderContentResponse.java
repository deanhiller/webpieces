package org.webpieces.router.api.dto;

import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;

public class RenderContentResponse {

	private byte[] payload;
	private int statusCode;
	private String reason;
	private MimeTypeResult mimeType;

	public RenderContentResponse(byte[] payload, int statusCode, String reason, MimeTypeResult mimeType) {
		this.payload = payload;
		this.statusCode = statusCode;
		this.mimeType = mimeType;
		this.reason = reason;
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

	public String getReason() {
		return reason;
	}

}

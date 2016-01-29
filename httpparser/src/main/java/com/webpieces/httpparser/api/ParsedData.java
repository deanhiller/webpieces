package com.webpieces.httpparser.api;

import com.webpieces.httpparser.api.dto.HttpMessage;

public class ParsedData {

	private ParsedStatus status;
	private HttpMessage msg;
	private byte[] leftOverData;
	
	public ParsedStatus getStatus() {
		return status;
	}
	public void setStatus(ParsedStatus status) {
		this.status = status;
	}
	public HttpMessage getMsg() {
		return msg;
	}
	public void setMsg(HttpMessage msg) {
		this.msg = msg;
	}
	public byte[] getLeftOverData() {
		return leftOverData;
	}
	public void setLeftOverData(byte[] leftOverData) {
		this.leftOverData = leftOverData;
	}

}

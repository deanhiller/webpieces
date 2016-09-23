package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.Http2Frame;

public class ParsedData {

	private ParsedStatus status;
	private Http2Frame msg;
	private DataWrapper leftOverData;
	
	public ParsedStatus getStatus() {
		return status;
	}
	public void setStatus(ParsedStatus status) {
		this.status = status;
	}
	public Http2Frame getMsg() {
		return msg;
	}
	public void setMsg(Http2Frame msg) {
		this.msg = msg;
	}
	
	public DataWrapper getLeftOverData() {
		return leftOverData;
	}
	public void setLeftOverData(DataWrapper leftOverData) {
		this.leftOverData = leftOverData;
	}

}

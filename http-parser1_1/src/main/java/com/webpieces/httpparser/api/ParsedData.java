package com.webpieces.httpparser.api;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.httpparser.api.dto.HttpPayload;

public class ParsedData {

	private ParsedStatus status;
	private HttpPayload msg;
	private DataWrapper leftOverData;
	
	public ParsedStatus getStatus() {
		return status;
	}
	public void setStatus(ParsedStatus status) {
		this.status = status;
	}
	public HttpPayload getMsg() {
		return msg;
	}
	public void setMsg(HttpPayload msg) {
		this.msg = msg;
	}
	
	public DataWrapper getLeftOverData() {
		return leftOverData;
	}
	public void setLeftOverData(DataWrapper leftOverData) {
		this.leftOverData = leftOverData;
	}

}

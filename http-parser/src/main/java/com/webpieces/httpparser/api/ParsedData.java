package com.webpieces.httpparser.api;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.httpparser.api.dto.HttpMessage;

public class ParsedData {

	private ParsedStatus status;
	private HttpMessage msg;
	private DataWrapper leftOverData;
	
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
	
	public DataWrapper getLeftOverData() {
		return leftOverData;
	}
	public void setLeftOverData(DataWrapper leftOverData) {
		this.leftOverData = leftOverData;
	}

}

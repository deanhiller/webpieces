package com.webpieces.httpparser.impl;

import java.util.List;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.dto.HttpMessage;

public class MementoImpl implements Memento {

	private DataWrapper data;
	private ParsedStatus status;
	private List<HttpMessage> parsedMessages;

	public void setStatus(ParsedStatus status) {
		this.status = status;
	}

	@Override
	public ParsedStatus getStatus() {
		return status;
	}

	@Override
	public List<HttpMessage> getParsedMessages() {
		return parsedMessages;
	}

	public void setParsedMessages(List<HttpMessage> parsedMessages) {
		this.parsedMessages = parsedMessages;
	}

	public DataWrapper getData() {
		return data;
	}

	public void setData(DataWrapper data) {
		this.data = data;
	}

}

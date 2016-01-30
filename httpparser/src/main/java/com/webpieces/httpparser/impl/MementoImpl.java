package com.webpieces.httpparser.impl;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.dto.HttpMessage;

public class MementoImpl implements Memento {

	private List<CachedData> data = new ArrayList<>();
	private ParsedStatus status;
	private List<HttpMessage> parsedMessage;

	public void setStatus(ParsedStatus status) {
		this.status = status;
	}

	@Override
	public ParsedStatus getStatus() {
		return status;
	}

	@Override
	public List<HttpMessage> getParsedMessages() {
		return parsedMessage;
	}

	public List<HttpMessage> getParsedMessage() {
		return parsedMessage;
	}

	public void setParsedMessage(List<HttpMessage> parsedMessage) {
		this.parsedMessage = parsedMessage;
	}

}

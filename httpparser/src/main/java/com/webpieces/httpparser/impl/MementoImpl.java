package com.webpieces.httpparser.impl;

import java.util.ArrayList;
import java.util.List;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.Memento;
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.dto.HttpMessage;

public class MementoImpl implements Memento {

	private List<Integer> leftOverMarkedPositions = new ArrayList<>();
	private DataWrapper leftOverData;
	private ParsedStatus status = ParsedStatus.NEED_MORE_DATA;
	private List<HttpMessage> parsedMessages = new ArrayList<>();

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

	public DataWrapper getLeftOverData() {
		return leftOverData;
	}

	public void setLeftOverData(DataWrapper data) {
		this.leftOverData = data;
	}

	public void addDemarcation(int i) {
		leftOverMarkedPositions.add(i);
	}

	public List<Integer> getLeftOverMarkedPositions() {
		return leftOverMarkedPositions;
	}

	public void setLeftOverMarkedPositions(List<Integer> leftOverMarkedPositions) {
		this.leftOverMarkedPositions = leftOverMarkedPositions;
	}
	
}

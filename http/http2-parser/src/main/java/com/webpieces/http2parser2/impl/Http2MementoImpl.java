package com.webpieces.http2parser2.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2ParsedStatus;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.Http2Frame;

public class Http2MementoImpl implements Http2Memento {

	private Decoder decoder;
	private Http2SettingsMap http2SettingsMap;

	public Http2MementoImpl(Decoder decoder, Http2SettingsMap http2SettingsMap) {
		this.decoder = decoder;
		this.http2SettingsMap = http2SettingsMap;
	}

	@Override
	public Http2ParsedStatus getParsedStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Http2Frame> getParsedMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataWrapper getLeftOverData() {
		// TODO Auto-generated method stub
		return null;
	}

}

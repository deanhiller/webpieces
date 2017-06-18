package com.webpieces.hpack.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.HpackConfig;
import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackStatefulParser;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class HpackStatefulParserImpl implements HpackStatefulParser {

	private HpackParser parser;
	private MarshalState marshalState;
	private UnmarshalState unmarshalState;

	public HpackStatefulParserImpl(HpackParser parser, HpackConfig config) {
		this.parser = parser;
		marshalState = this.parser.prepareToMarshal(config.getMaxHeaderTableSize(), config.getRemoteMaxFrameSize());
		unmarshalState = this.parser.prepareToUnmarshal(config.getLogId(), config.getMaxHeaderSize(), config.getMaxHeaderTableSize(), config.getLocalMaxFrameSize());
	}

	@Override
	public UnmarshalState unmarshal(DataWrapper newData) {
		return parser.unmarshal(unmarshalState, newData);
	}

	@Override
	public DataWrapper marshal(Http2Msg frame) {
		return parser.marshal(marshalState, frame);
	}

	@Override
	public List<Http2Setting> unmarshalSettingsPayload(String base64SettingsPayload) {
		return parser.unmarshalSettingsPayload(base64SettingsPayload);
	}

	@Override
	public String marshalSettingsPayload(List<Http2Setting> settings) {
		return parser.marshalSettingsPayload(settings);
	}

}

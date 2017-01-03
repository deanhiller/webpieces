package org.webpieces.httpcommon.temp;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public class TempHttp2ParserImpl implements TempHttp2Parser {

	private Http2Parser parser;

	public ParserResult prepareToParse() {
		return parser.prepareToParse();
	}

	public ParserResult parse(ParserResult oldData, DataWrapper newData, Decoder decoder, List<Http2Setting> settings) {
		return parser.parse(oldData, newData, decoder, settings);
	}

	public DataWrapper marshal(Http2Frame frame) {
		return parser.marshal(frame);
	}

	public SettingsFrame unmarshalSettingsPayload(ByteBuffer settingsPayload) {
		return parser.unmarshalSettingsPayload(settingsPayload);
	}

	public Http2Frame unmarshal(DataWrapper data) {
		return parser.unmarshal(data);
	}

	public int getFrameLength(Http2Frame frame) {
		return parser.getFrameLength(frame);
	}

	public TempHttp2ParserImpl(Http2Parser parser) {
		this.parser = parser;
	}

}

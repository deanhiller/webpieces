package org.webpieces.httpcommon.temp;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.hpack.impl.HeaderDecoding;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderDecoding2 {

	private HeaderDecoding decoding = new HeaderDecoding();
	private Decoder decoder;
	
	public HeaderDecoding2(Decoder decoder) {
		this.decoder = decoder;
	}

	public List<Http2Header> decode(DataWrapper allSerializedHeaders) {
		return decoding.decode(decoder, allSerializedHeaders);
	}

}

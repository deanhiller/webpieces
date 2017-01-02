package com.webpieces.http2engine.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderDecoding {

	private Decoder decoder;

	public HeaderDecoding(Decoder decoder) {
		this.decoder = decoder;
	}

	public List<Http2Header> decode(DataWrapper data) {
        List<Http2Header> headers = new ArrayList<>();

        byte[] bytes = data.createByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            decoder.decode(in, (name, value, sensitive) -> {
                        String h = new String(name);
                        String v = new String(value);
                        if(!h.equals(h.toLowerCase())) {
                            throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR);
                        }
                        headers.add(new Http2Header(h, v));
                    }
            );
        } catch (IOException e) {
            // TODO: this doesn't catch the h2spec -s 4.3 invalid header block fragment
            throw new ParseException(Http2ErrorCode.COMPRESSION_ERROR);
        }
        decoder.endHeaderBlock();
        return headers;
    }
}

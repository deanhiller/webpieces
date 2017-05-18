package com.webpieces.hpack.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.ConnectionException;
import com.webpieces.http2parser.api.ParseFailReason;
import com.webpieces.http2parser.api.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderDecoding {

	public void setMaxHeaderTableSize(Decoder decoder, int newSize) {
		synchronized(decoder) {
			decoder.setMaxHeaderTableSize(newSize);
		}
	}

	public List<Http2Header> decode(Decoder decoder, DataWrapper data, int streamId) {
		try {
			return decodeImpl(decoder, data, streamId);
        } catch (IOException e) {
            // TODO: this doesn't catch the h2spec -s 4.3 invalid header block fragment
            throw new ConnectionException(ParseFailReason.HEADER_DECODE, streamId, "Error from hpack library", e);
            //TODO: clone hpack and fix so they throw with debug info as their errors contain no info :(
        }
	}
	
	private List<Http2Header> decodeImpl(Decoder decoder, DataWrapper data, int streamId) throws IOException {
        List<Http2Header> headers = new ArrayList<>();
        byte[] bytes = data.createByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        //keep this synchronized very very small...
		synchronized(decoder) {
	        decoder.decode(in, (n, v, s) -> addToHeaders(headers, n, v, s, streamId));
	        decoder.endHeaderBlock();
	        return headers;
		}
    }

	private Object addToHeaders(List<Http2Header> headers, byte[] name, byte[] value, boolean sensitive, int streamId) {
        String h = new String(name);
        String v = new String(value);
        if(!h.equals(h.toLowerCase()))
            throw new StreamException(ParseFailReason.HEADER_NOT_LOWER_CASE, streamId, "header="+h+" was not lower case in stream="+streamId);
        
        headers.add(new Http2Header(h, v));

		return null;
	}

}

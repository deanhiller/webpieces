package com.webpieces.hpack.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2ParseException;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderDecoding {

	public void setMaxHeaderTableSize(HpackMementoImpl state, int newSize) {
		Decoder decoder = state.getDecoder();
		
		synchronized(decoder) {
			decoder.setMaxHeaderTableSize(newSize);
		}
	}

	public List<Http2Header> decode(Decoder decoder, DataWrapper data) {
		try {
			return decodeImpl(decoder, data);
        } catch (IOException e) {
            // TODO: this doesn't catch the h2spec -s 4.3 invalid header block fragment
            throw new Http2ParseException(Http2ErrorCode.COMPRESSION_ERROR);
        }			
	}
	
	private List<Http2Header> decodeImpl(Decoder decoder, DataWrapper data) throws IOException {
        List<Http2Header> headers = new ArrayList<>();
        byte[] bytes = data.createByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        //keep this synchronized very very small...
		synchronized(decoder) {
	        decoder.decode(in, (n, v, s) -> addToHeaders(headers, n, v, s));
	        decoder.endHeaderBlock();
	        return headers;
		}
    }

	private Object addToHeaders(List<Http2Header> headers, byte[] name, byte[] value, boolean sensitive) {
        String h = new String(name);
        String v = new String(value);
        if(!h.equals(h.toLowerCase()))
            throw new Http2ParseException(Http2ErrorCode.PROTOCOL_ERROR);
        
        headers.add(new Http2Header(h, v));

		return null;
	}

}

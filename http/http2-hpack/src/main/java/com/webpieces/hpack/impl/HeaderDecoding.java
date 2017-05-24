package com.webpieces.hpack.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.ParseFailReason;
import com.webpieces.http2parser.api.dto.error.StreamException;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class HeaderDecoding {

	public void setMaxHeaderTableSize(Decoder decoder, int newSize) {
		synchronized(decoder) {
			decoder.setMaxHeaderTableSize(newSize);
		}
	}

	public List<Http2Header> decode(Decoder decoder, DataWrapper data, int streamId, Consumer<Http2Header> knownHeaders) {
		try {
			return decodeImpl(decoder, data, streamId, knownHeaders);
        } catch (IOException e) {
            // TODO: this doesn't catch the h2spec -s 4.3 invalid header block fragment
            throw new ConnectionException(ParseFailReason.HEADER_DECODE, streamId, "Error from hpack library", e);
            //TODO: clone hpack and fix so they throw with debug info as their errors contain no info :(
        }
	}
	
	private List<Http2Header> decodeImpl(Decoder decoder, DataWrapper data, int streamId, Consumer<Http2Header> knownHeaders) throws IOException {
        List<Http2Header> headers = new ArrayList<>();
        byte[] bytes = data.createByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        	
        //keep this synchronized very very small...
		synchronized(decoder) {
	        decoder.decode(in, (n, v, s) -> addToHeaders(headers, knownHeaders, n, v, s, streamId));
	        decoder.endHeaderBlock();
		}
	
        if(data.getReadableSize() > 0 && headers.size() == 0)
        	throw new ConnectionException(ParseFailReason.COMPRESSION_ERROR, streamId, "Header data came in, but no headers came out");

        return headers;
    }

	private Object addToHeaders(
			List<Http2Header> headers, Consumer<Http2Header> knownHeaders, 
			byte[] name, byte[] value, boolean sensitive, int streamId) {
        String h = new String(name);
        String v = new String(value);
        if(!h.equals(h.toLowerCase()))
            throw new StreamException(ParseFailReason.HEADER_NOT_LOWER_CASE, streamId, "header="+h+" was not lower case in stream="+streamId);
        
        Http2Header header = new Http2Header(h, v);
        headers.add(header);
        
        if(knownHeaders != null) {
        	Http2HeaderName knownName = Http2HeaderName.lookup(h);
        	if(knownName != null)
        		knownHeaders.accept(header);
        }
		return null;
	}

}

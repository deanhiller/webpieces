package com.webpieces.hpack.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public interface HpackStatefulParser {
	
    UnmarshalState unmarshal(DataWrapper newData);

    DataWrapper marshal(Http2Msg msg);
    
    /**
     * Unfortunately, in the http1.1 request to a server, the base64 http/2 upgrade settings header ONLY
     * contains the 'payload' of a SettingFrame, so we must have a method to just parse the payload of a
     * settings frame so this is a one-off function that I don't like exposing but need to. 
     */
	List<Http2Setting> unmarshalSettingsPayload(String base64SettingsPayload);
	
	/**
	 * Base 64 of the 'payload' of the SettingsFrame only, excluding the frame piece
	 */
	String marshalSettingsPayload(List<Http2Setting> settings);
	
}

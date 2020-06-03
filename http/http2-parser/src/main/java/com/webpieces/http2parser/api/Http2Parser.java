package com.webpieces.http2parser.api;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Setting;

public interface Http2Parser {
	
	//taking a stab at my guess of what we could do instead
	Http2Memento prepareToParse(long maxFrameSize); 
	
	Http2Memento parse(Http2Memento memento, DataWrapper newData);

	DataWrapper marshal(Http2Frame frame);
	
    /**
     * Unfortunately, in the http1.1 request to a server, the base64 http/2 upgrade settings header ONLY
     * contains the payload of a SettingFrame, so we must have a method to just parse the payload of a
     * settings frame so this is a one-off function that I don't like exposing but need to. 
     */
	List<Http2Setting> unmarshalSettingsPayload(String base64SettingsPayload);

	/**
	 * Return base64 encoded Settings payload
	 */
	String marshalSettingsPayload(List<Http2Setting> settingsPayload);

}

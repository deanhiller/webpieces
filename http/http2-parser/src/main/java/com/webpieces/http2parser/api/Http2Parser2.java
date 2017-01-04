package com.webpieces.http2parser.api;

import java.nio.ByteBuffer;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface Http2Parser2 {
	
	//taking a stab at my guess of what we could do instead
	Http2Memento prepareToParse(); 
	
	Http2Memento parse(Http2Memento memento, DataWrapper newData, long maxFrameSize);

	DataWrapper marshal(Http2Frame frame);
	
    /**
     * Unfortunately, in the http1.1 request to a server, the base64 http/2 upgrade settings header ONLY
     * contains the payload of a SettingFrame, so we must have a method to just parse the payload of a
     * settings frame so this is a one-off function that I don't like exposing but need to. 
     */
	SettingsFrame unmarshalSettingsPayload(ByteBuffer settingsPayload);

}

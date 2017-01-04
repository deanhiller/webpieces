package org.webpieces.httpcommon.temp;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public interface TempHttp2Parser {

    ParserResult prepareToParse();
    ParserResult parse(ParserResult oldData, DataWrapper newData, Decoder decoder, List<Http2Setting> settings);

    DataWrapper marshal(Http2Frame frame);

    /**
     * Unfortunately, in the http1.1 request to a server, the base64 http/2 upgrade settings header ONLY
     * contains the 'payload' of a SettingFrame, so we must have a method to just parse the payload of a
     * settings frame so this is a one-off function that I don't like exposing but need to. 
     */
	SettingsFrame unmarshalSettingsPayload(ByteBuffer settingsPayload);

    // TODO: add a marshal to bytebuffer so we can use our bufferpool
    @Deprecated
    int getFrameLength(Http2Frame frame);
    
}

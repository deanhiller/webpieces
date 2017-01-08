package org.webpieces.httpcommon.temp;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Setting;

public interface TempHttp2Parser {

    Http2Memento prepareToParse();
    Http2Memento parse(Http2Memento oldData, DataWrapper newData, Decoder decoder, List<Http2Setting> settings);

    DataWrapper marshal(Http2Frame frame);

    /**
     * Unfortunately, in the http1.1 request to a server, the base64 http/2 upgrade settings header ONLY
     * contains the 'payload' of a SettingFrame, so we must have a method to just parse the payload of a
     * settings frame so this is a one-off function that I don't like exposing but need to. 
     */
	List<Http2Setting> unmarshalSettingsPayload(String base64SettingsPayload);

}

package org.webpieces.httpcommon.api;

import com.webpieces.http2parser.api.dto.Http2Settings;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

import java.util.concurrent.CompletableFuture;

public interface Http2Engine {
    enum HttpSide { CLIENT, SERVER }

    void startPing();

    DataListener getDataListener();

    Http2Settings getLocalRequestedSettingsFrame();

    void cleanUpPendings(String msg);

    Channel getUnderlyingChannel();

    void sendLocalRequestedSettings();

    void setRemoteSettings(Http2Settings frame, boolean sendAck);
}

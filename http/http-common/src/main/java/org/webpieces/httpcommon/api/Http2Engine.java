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

    /**
     * Starts a timer to send a ping to the remote side every 5 seconds, to measure latency.
     *
     */
    void startPing();


    /**
     * Gets the datalistener that's active for the engine.
     *
     * @return
     */
    DataListener getDataListener();


    /**
     * Gets the Channel that this engine is running on.
     *
     * @return
     */
    Channel getUnderlyingChannel();


    /**
     * Send the settings that we want to the remote side. Note that this doesn't
     * actually set those settings until we receive an 'ack' for those settings.
     *
     * TODO: When we set settings using the HTTP2-Settings header in an upgrade request, set our local settings appropriately.
     * (i.e. don't want for the ack)
     */
    void sendLocalRequestedSettings();
}

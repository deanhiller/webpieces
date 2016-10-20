package org.webpieces.frontend.api;

import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.nio.api.handlers.DataListener;

import java.nio.ByteBuffer;
import java.util.Optional;

public interface HttpServerSocket extends HttpSocket {
    ResponseSender getResponseSender();

    DataListener getDataListener();

    void upgradeHttp2(Optional<ByteBuffer> maybeSettingsFrame);
}

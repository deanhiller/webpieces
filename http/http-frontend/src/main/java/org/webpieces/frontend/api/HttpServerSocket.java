package org.webpieces.frontend.api;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.nio.api.handlers.DataListener;

public interface HttpServerSocket extends HttpSocket {
    ResponseSender getResponseSender();

    DataListener getDataListener();

    void upgradeHttp2(Optional<ByteBuffer> maybeSettingsFrame);

    void sendLocalRequestedSettings();
}

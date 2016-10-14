package org.webpieces.httpcommon.api;

import com.webpieces.http2parser.api.dto.Http2Settings;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.nio.api.handlers.DataListener;

import java.util.concurrent.CompletableFuture;

public interface Http2Engine {
    enum HttpSide { CLIENT, SERVER }

    void initialize();

    DataListener getDataListener();

    Http2Settings getLocalRequestedSettingsFrame();

    CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete);

    void cleanUpPendings(String msg);

    //Client Only
    // These are both needed to give the RequestSender the ability to deal with negotiating http1.1 vs http2.
    RequestId createInitialStream(HttpResponse r, HttpRequest req, ResponseListener listener, DataWrapper leftOverData);
    void sendHttp2Preface();
    CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener l);
}

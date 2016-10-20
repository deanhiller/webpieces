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

    void initialize();

    DataListener getDataListener();

    Http2Settings getLocalRequestedSettingsFrame();

    void cleanUpPendings(String msg);

    Channel getUnderlyingChannel();

    void sendLocalPreferredSettings();

    void setRemoteSettings(Http2Settings frame);

    //Client Only
    // These are both needed to give the RequestSender the ability to deal with negotiating http1.1 vs http2.
    RequestId createInitialStream(HttpResponse r, HttpRequest req, ResponseListener listener, DataWrapper leftOverData);
    void sendHttp2Preface();
    CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener l);
    CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete);

    //Server only
    // Can be used to initiate multiple responses to the same requestid, but the 'request' that comes back
    // for the second and future response are different from the first response, they are the 'assumed request'
    // that this 'push' response is associated with.
    CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete);
    CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isComplete);
    ResponseSender getResponseSender();
    void setRequestListener(RequestListener requestListener);


}

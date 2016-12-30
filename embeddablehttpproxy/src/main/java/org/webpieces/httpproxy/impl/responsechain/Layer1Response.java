package org.webpieces.httpproxy.impl.responsechain;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class Layer1Response implements ResponseListener {

	private Layer2ResponseListener responseListener;
	private ResponseSender responseSender;
	private HttpRequest req;
	private RequestId requestId;

	public Layer1Response(Layer2ResponseListener responseListener, ResponseSender responseSender, HttpRequest req) {
		this.responseListener = responseListener;
		this.responseSender = responseSender;
		this.req = req;
	}

    public void setRequestId(RequestId requestId) {
        this.requestId = requestId;
    }

    @Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId responseId, boolean isComplete) {
		responseListener.processResponse(responseSender, req, resp, requestId, responseId, isComplete);

	}

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isLastData) {
		return responseListener.processData(responseSender, data, id, isLastData);
    }

    @Override
    public void incomingTrailer(List<Http2Header> headers, ResponseId id, boolean isComplete) {
        // TODO: Handle trailers
        throw new UnsupportedOperationException();
    }

    @Override
	public void failure(Throwable e) {
		responseListener.processError(responseSender, req, e);
	}

}

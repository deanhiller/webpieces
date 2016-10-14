package org.webpieces.httpproxy.impl.responsechain;

import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpClientException;
import org.webpieces.httpcommon.api.exceptions.HttpServerException;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.httpproxy.impl.chain.LayerZSendBadResponse;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class Layer2ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(Layer2ResponseListener.class);
	private ConcurrentHashMap<ResponseId, ResponseId> incomingResponseToOutgoingResponseMap = new ConcurrentHashMap<>();

	@Inject
	private LayerZSendBadResponse badResponse;
	
	public void processResponse(ResponseSender responseSender, HttpRequest req, HttpResponse resp, ResponseId incomingResponseId, boolean isComplete) {
		log.info("received response(responseSender="+responseSender+").  type="+resp.getClass().getSimpleName()+" complete="+isComplete+" resp=\n"+resp);


		responseSender.sendResponse(resp, req, isComplete)
			.thenAccept(outgoingResponseId -> {
				wroteBytes(responseSender);
				incomingResponseToOutgoingResponseMap.put(incomingResponseId, outgoingResponseId);
			})
			.exceptionally(e -> failedWrite(responseSender, e));
	}

	public CompletableFuture<Void> processData(ResponseSender responseSender, DataWrapper data, ResponseId incomingResponseId, boolean isComplete) {
		ResponseId outgoingResponseId = incomingResponseToOutgoingResponseMap.get(incomingResponseId);
		return responseSender.sendData(data, outgoingResponseId, isComplete);
	}

	private Void failedWrite(ResponseSender responseSender, Throwable e) {
		log.error("failed to respond to responseSender="+responseSender, e);
		return null;
	}

	private void wroteBytes(ResponseSender responseSender) {
		log.info("wrote bytes out responseSender="+responseSender);
	}

	public Void processError(ResponseSender responseSender, HttpRequest req, Throwable e) {
		log.error("could not process req="+req+" from responseSender="+responseSender+" due to exception", e);

		if(e.getCause() instanceof UnresolvedAddressException) {
			HttpClientException exc = new HttpClientException("Client gave a bad address to connect to", KnownStatusCode.HTTP_404_NOTFOUND, e);
			badResponse.sendServerResponse(responseSender, exc);
		} else {
			HttpServerException exc = new HttpServerException("Server has a bug", KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, e);
			badResponse.sendServerResponse(responseSender, exc);
		}
		
		responseSender.close();
		
		return null;
	}

	public void farEndClosed(HttpClientSocket socket, Channel channel) {
		//since socket is closing, closeSocket the channel from the browser...
		log.info("closing connection from browser.  channel="+channel);
		channel.close();
	}

}

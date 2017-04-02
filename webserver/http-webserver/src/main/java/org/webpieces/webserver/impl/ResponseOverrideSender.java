package org.webpieces.webserver.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.OverwritePlatformResponse;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class ResponseOverrideSender {

	private ResponseSender responseSender;

	public ResponseOverrideSender(ResponseSender responseSender) {
		this.responseSender = responseSender;
	}

	@Override
	public String toString() {
		return "ResponseOverrideSender [responseSender=" + responseSender + "]";
	}

	public CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete) {
		//in some exceptional cases like incoming cookies failing to parse, there will be no context
		HttpResponse finalResp = response;
		if(Current.isContextSet()) {
			List<OverwritePlatformResponse> callbacks = Current.getContext().getCallbacks();
			for(OverwritePlatformResponse callback : callbacks) {
				finalResp = (HttpResponse)callback.modifyOrReplace(finalResp);
			}
		}
		
		return responseSender.sendResponse(finalResp, request, requestId, isComplete);
	}

	public CompletableFuture<Void> close() {
		return responseSender.close();
	}

	public CompletableFuture<Void> sendData(DataWrapper data, ResponseId responseId, boolean isComplete) {
		return responseSender.sendData(data, responseId, isComplete);
	}


}

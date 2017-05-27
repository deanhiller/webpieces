package org.webpieces.webserver.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.OverwritePlatformResponse;
import org.webpieces.frontend2.api.ResponseStream;
import org.webpieces.frontend2.impl.ProtocolType;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;

public class ResponseOverrideSender {

	private ResponseStream stream;

	public ResponseOverrideSender(ResponseStream stream) {
		this.stream = stream;
	}

	@Override
	public String toString() {
		return "ResponseOverrideSender [responseSender=" + stream + "]";
	}

	public CompletableFuture<StreamWriter> sendResponse(Http2Response response) {
		//in some exceptional cases like incoming cookies failing to parse, there will be no context
		Http2Response finalResp = response;
		if(Current.isContextSet()) {
			List<OverwritePlatformResponse> callbacks = Current.getContext().getCallbacks();
			for(OverwritePlatformResponse callback : callbacks) {
				finalResp = (Http2Response)callback.modifyOrReplace(finalResp);
			}
		}
		
		return stream.sendResponse(finalResp);
	}

	public void close() {
		if(stream.getSocket().getProtocol() == ProtocolType.HTTP1_1)
			stream.getSocket().close("Connection KeepAlive not set");
	}

}

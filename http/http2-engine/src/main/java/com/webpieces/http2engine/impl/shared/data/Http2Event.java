package com.webpieces.http2engine.impl.shared.data;

import java.util.HashMap;
import java.util.Map;

public enum Http2Event {

	SENT_HEADERS(Http2SendRecieve.SEND, Http2PayloadType.HEADERS),
	SENT_HEADERS_EOS(Http2SendRecieve.SEND, Http2PayloadType.HEADERS_EOS),

	RECV_HEADERS(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS),
	RECV_HEADERS_EOS(Http2SendRecieve.RECEIVE, Http2PayloadType.HEADERS_EOS),

	SENT_PUSH(Http2SendRecieve.SEND, Http2PayloadType.PUSH_PROMISE),
	RECV_PUSH(Http2SendRecieve.RECEIVE, Http2PayloadType.PUSH_PROMISE),

	SENT_RST(Http2SendRecieve.SEND, Http2PayloadType.RESET_STREAM),		
	RECV_RST(Http2SendRecieve.RECEIVE, Http2PayloadType.RESET_STREAM),
	
	SENT_DATA(Http2SendRecieve.SEND, Http2PayloadType.DATA),
	SENT_DATA_EOS(Http2SendRecieve.SEND, Http2PayloadType.DATA_EOS),
	
	RECV_DATA(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA),
	RECV_DATA_EOS(Http2SendRecieve.RECEIVE, Http2PayloadType.DATA_EOS);
	
	private static Map<Http2PayloadType, Http2Event> sentMap = new HashMap<>();
	private static Map<Http2PayloadType, Http2Event> recvMap = new HashMap<>();
	
	static {
		for(Http2Event evt : Http2Event.values()) {
			if(evt.sendReceive == Http2SendRecieve.RECEIVE)
				recvMap.put(evt.payloadType, evt);
			else
				sentMap.put(evt.payloadType, evt);
		}
	}
	
	private Http2SendRecieve sendReceive;
	private Http2PayloadType payloadType;

	private Http2Event(Http2SendRecieve sendReceive, Http2PayloadType payloadType) {
		this.sendReceive = sendReceive;
		this.payloadType = payloadType;
	}

	public static enum Http2SendRecieve {
		SEND, RECEIVE
	}

	public static Http2Event lookup(Http2SendRecieve sendRecv, Http2PayloadType payloadType) {
		if(sendRecv == Http2SendRecieve.RECEIVE)
			return recvMap.get(payloadType);
		else
			return sentMap.get(payloadType);
	}
	
}

package com.webpieces.http2engine.impl.shared;

public class Http2Event {

	private Http2SendRecieve sendReceive;
	private Http2PayloadType payloadType;

	public Http2Event(Http2SendRecieve sendReceive, Http2PayloadType payloadType) {
		this.sendReceive = sendReceive;
		this.payloadType = payloadType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((payloadType == null) ? 0 : payloadType.hashCode());
		result = prime * result + ((sendReceive == null) ? 0 : sendReceive.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Http2Event other = (Http2Event) obj;
		if (payloadType != other.payloadType)
			return false;
		if (sendReceive != other.sendReceive)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "Http2Event [sendReceive=" + sendReceive + ", payloadType=" + payloadType + "]";
	}


	public static enum Http2SendRecieve {
		SEND, RECEIVE
	}
}

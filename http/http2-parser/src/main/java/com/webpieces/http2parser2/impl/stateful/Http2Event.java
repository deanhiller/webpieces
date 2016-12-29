package com.webpieces.http2parser2.impl.stateful;

public class Http2Event {

	private Http2SendRecieve type;
	private Class<?> payloadType;

	public Http2Event(Http2SendRecieve type, Class<?> payloadType) {
		this.type = type;
		this.payloadType = payloadType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((payloadType == null) ? 0 : payloadType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (payloadType == null) {
			if (other.payloadType != null)
				return false;
		} else if (!payloadType.equals(other.payloadType))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public static enum Http2SendRecieve {
		SEND, RECEIVE
	}
}

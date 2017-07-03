package org.webpieces.ssl.api.dto;

public enum SslActionEnum {

	SEND_TO_SOCKET,
	SEND_TO_APP,
	WAIT_FOR_MORE_DATA_FROM_REMOTE_END,
	SEND_LINK_ESTABLISHED_TO_APP,
	SEND_LINK_CLOSED_TO_APP
}

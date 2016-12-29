package com.webpieces.http2parser.api.highlevel;

import org.webpieces.data.api.DataWrapper;

public interface ToSocket {

	void sendToSocket(DataWrapper newData);

}

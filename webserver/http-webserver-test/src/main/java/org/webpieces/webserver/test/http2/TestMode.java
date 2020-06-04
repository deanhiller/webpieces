package org.webpieces.webserver.test.http2;

public enum TestMode {

	//avoids marshalling in client, http2 engine, hpack etc. 
	EMBEDDED_DIRET_NO_PARSING,
	EMBEDDED_PARSING,
	REMOTE
}

package org.webpieces.frontend2.impl;

public enum InitiationStatus {
	PREFACE, //someone just assumes we are http2 (and guess what, we are).  This is optimal for apis that can document they are http2
	HTTP2_UPGRADE_REQUEST, //Uses a http 1.1 request with 'Upgrade: h2c' header
	HTTP1_1 //we found out this is simply an http1.1 request :(

}

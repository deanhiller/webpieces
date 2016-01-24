package com.webyoso.httpparser.api;

public enum HttpMethod {

	//TODO: Let's figure out what extension-method is as I think we need
	//to allow for an unknow method here that can be passed through in the case
	//of a proxy
	OPTIONS,
	GET,
	HEAD,
	POST,
	PUT,
	DELETE,
	TRACE,
	CONNECT
	;
	
}

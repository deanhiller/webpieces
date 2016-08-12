package org.webpieces.router.impl.params;

public enum FromEnum {

	//params like /account/{id} where the incoming was /account/5444
	URL_PATH,
	//params like /account/something?id=5444
	QUERY_PARAM,
	//params from a POST request from a form
	FORM_MULTIPART;
}

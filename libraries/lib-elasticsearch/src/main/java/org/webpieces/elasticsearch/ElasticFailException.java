package org.webpieces.elasticsearch;

import org.elasticsearch.client.Response;

public class ElasticFailException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Response response;

	public ElasticFailException(String msg, Response response) {
		super(msg);
		this.response = response;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}


}

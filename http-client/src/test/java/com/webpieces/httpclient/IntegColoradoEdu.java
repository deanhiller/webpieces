package com.webpieces.httpclient;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpSocket;

import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpRequestMethod;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpUri;

public class IntegColoradoEdu {

	private static final Logger log = LoggerFactory.getLogger(IntegColoradoEdu.class);
	
	public static void main(String[] args) {
//		GET / HTTP/1.1
//		Host: www.colorado.edu
//		User-Agent: curl/7.43.0
//		Accept: */*
		
		String host = "www.colorado.edu";
		int port = 80;

		HttpRequestLine requestLine = new HttpRequestLine();
		HttpRequestMethod method = HttpRequestMethod.GET;
		requestLine.setMethod(method );
		requestLine.setUri(new HttpUri("/"));
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		req.addHeader(new Header(KnownHeaderName.HOST, host));
		req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
		req.addHeader(new Header(KnownHeaderName.USER_AGENT, "webpieces/0.9"));

		HttpClientFactory factory = HttpClientFactory.createFactory();
		HttpClient client = factory.createHttpClient();
		HttpSocket socket = client.openHttpSocket("oneTimer");
		socket
			.connect(new InetSocketAddress(host, port))
			.thenCompose(p -> socket.send(req))
			.thenAccept(resp -> processResp(socket, resp))
			.exceptionally(e -> reportException(socket, e));
	}

	private static Void reportException(HttpSocket socket, Throwable e) {
		log.warn("exception on socket="+socket, e);
		return null;
	}

	private static void processResp(HttpSocket socket, HttpResponse resp) {
		log.info("received resp="+resp);
		//socket.closeSocket();
		//System.exit(0);
	}
}

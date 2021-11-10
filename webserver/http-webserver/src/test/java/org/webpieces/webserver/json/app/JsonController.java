package org.webpieces.webserver.json.app;

import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugin.json.Jackson;
import org.webpieces.http.exception.NotFoundException;

import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

@Singleton
public class JsonController {
	
	private FakeAuthService svc;
	private EchoStreamingClient client;

	@Inject
	public JsonController(FakeAuthService svc, EchoStreamingClient client) {
		this.svc = svc;
		this.client = client;
	}

	public StreamRef streaming(ResponseStreamHandle handle) {
		XFuture<StreamRef> futureStream = new XFuture<>();

		XFuture<Boolean> authFuture = svc.authenticate("bobsmith");
		XFuture<StreamWriter> writer = authFuture.thenCompose(resp -> {
			StreamRef streamRef = client.stream(handle);
			futureStream.complete(streamRef);
			return streamRef.getWriter();
		});

		return new StreamRefProxy(writer, futureStream);
	}
	
	public SearchResponse simple(@Jackson SearchRequest request) {
		SearchMeta meta = new SearchMeta();
		meta.setExtraField("");
		
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(99);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		resp.setMeta(meta);
		//leave resp.summary null for test!!!
		svc.saveRequest(request);
		
		resp.setSummary("");
	
		return resp;
	}
	
	
	
	public XFuture<SearchResponse> asyncJsonRequest(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(8);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return XFuture.completedFuture(resp);
	}
	
	public SearchResponse jsonRequest(int id, @Jackson SearchRequest request) {
		
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(5);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	public SearchResponse postJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(99);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	public XFuture<SearchResponse> postAsyncJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(98);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return XFuture.completedFuture(resp);
	}
	
	@Jackson
	public SearchResponse readOnly() {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(1);
		return resp;
	}

	public void writeOnly(@Jackson SearchRequest request) {
		
	}

	public XFuture<Void> writeAsync(@Jackson SearchRequest request) {
		return XFuture.completedFuture(null);
	}
	
	public SearchResponse throwNotFound(int id, @Jackson SearchRequest request) {
		throw new NotFoundException("to test it out");
	}
}

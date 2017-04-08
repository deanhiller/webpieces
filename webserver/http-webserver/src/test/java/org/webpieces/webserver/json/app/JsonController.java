package org.webpieces.webserver.json.app;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import org.webpieces.plugins.json.Jackson;
import org.webpieces.router.api.exceptions.NotFoundException;

@Singleton
public class JsonController {
	
	public CompletableFuture<SearchResponse> asyncJsonRequest(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(8);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return CompletableFuture.completedFuture(resp);
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
	
	public CompletableFuture<SearchResponse> postAsyncJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(98);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return CompletableFuture.completedFuture(resp);
	}
	
	public SearchResponse throwNotFound(int id, @Jackson SearchRequest request) {
		throw new NotFoundException("to test it out");
	}
}

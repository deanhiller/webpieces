package org.webpieces.webserver.json.app;

import javax.inject.Singleton;

import org.webpieces.plugins.json.Jackson;

@Singleton
public class JsonController {
	
	public SearchResponse jsonRequest(int id, @Jackson SearchRequest request) {
		
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(5);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}

	public SearchResponse postJson(int id, @Jackson SearchRequest request) {
		
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(5);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
}

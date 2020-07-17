package org.webpieces.webserver.json.app;

import java.util.ArrayList;
import java.util.List;

//@Jackson
public class SearchResponse {

	private int searchTime;
	private List<String> matches = new ArrayList<>();
	private String summary;
	private SearchMeta meta;
	
	public int getSearchTime() {
		return searchTime;
	}
	public void setSearchTime(int searchTime) {
		this.searchTime = searchTime;
	}
	public List<String> getMatches() {
		return matches;
	}
	public void setMatches(List<String> matches) {
		this.matches = matches;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public SearchMeta getMeta() {
		return meta;
	}
	public void setMeta(SearchMeta meta) {
		this.meta = meta;
	}
	
	
	
}

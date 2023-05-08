package webpiecesxxxxxpackage.deleteme.api;

import java.util.ArrayList;
import java.util.List;

//@Jackson
public class SaveResponse {

	private int searchTime;
	private Boolean success;
	private List<TheMatch> matches = new ArrayList<>();
	
	public int getSearchTime() {
		return searchTime;
	}
	public void setSearchTime(int searchTime) {
		this.searchTime = searchTime;
	}

	public List<TheMatch> getMatches() {
		return matches;
	}

	public void setMatches(List<TheMatch> matches) {
		this.matches = matches;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}

package WEBPIECESxPACKAGE.json;

public class SearchRequest {

	private SearchMeta meta;
	private String query;

	public SearchMeta getMeta() {
		return meta;
	}
	public void setMeta(SearchMeta meta) {
		this.meta = meta;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
}

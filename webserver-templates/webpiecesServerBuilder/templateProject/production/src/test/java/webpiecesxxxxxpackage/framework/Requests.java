package webpiecesxxxxxpackage.framework;

import webpiecesxxxxxpackage.json.SearchRequest;

public class Requests {
    public static SearchRequest createSearchRequest() {
        SearchRequest req = new SearchRequest();
        req.setQuery("my query");
        return req;
    }
}

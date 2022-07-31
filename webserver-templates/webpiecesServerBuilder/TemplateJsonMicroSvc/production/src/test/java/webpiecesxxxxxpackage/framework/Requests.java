package webpiecesxxxxxpackage.framework;

import webpiecesxxxxxpackage.deleteme.api.SearchRequest;

public class Requests {
    public static SearchRequest createSearchRequest() {
        SearchRequest req = new SearchRequest();
        req.setQuery("my query");
        return req;
    }
}

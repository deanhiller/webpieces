package webpiecesxxxxxpackage.framework;

import webpiecesxxxxxpackage.deleteme.api.SaveRequest;

public class Requests {
    public static SaveRequest createSearchRequest() {
        SaveRequest req = new SaveRequest();
        req.setQuery("my query");
        return req;
    }
}

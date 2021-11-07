package org.webpieces.webserver.routing.app;

import org.webpieces.plugin.json.Jackson;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.webserver.json.app.SearchRequest;
import org.webpieces.webserver.json.app.SearchResponse;

import javax.inject.Singleton;

@Singleton
public class ControllerForTestOptions {

    public SearchResponse getContent(@Jackson SearchRequest request) {
        return new SearchResponse();
    }

    public SearchResponse postContent(@Jackson SearchRequest request) {
        return new SearchResponse();
    }

    public SearchResponse deleteContent(@Jackson SearchRequest request) {
        return new SearchResponse();
    }

    public SearchResponse putContent(@Jackson SearchRequest request) {
        return new SearchResponse();
    }

}

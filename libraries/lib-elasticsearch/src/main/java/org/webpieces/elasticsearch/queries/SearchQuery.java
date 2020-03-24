package org.webpieces.elasticsearch.queries;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public class SearchQuery {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Query query;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Suggestion> suggest;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Map<String, Suggestion> getSuggest() {
        return suggest;
    }

    public void setSuggest(Map<String, Suggestion> suggest) {
        this.suggest = suggest;
    }
}

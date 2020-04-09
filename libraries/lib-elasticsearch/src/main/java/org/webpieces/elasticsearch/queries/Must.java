package org.webpieces.elasticsearch.queries;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Must {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("match_all")
    private Map<String, String> matchAll;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Match match;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> term;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Query query;

    @JsonProperty("query_string")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private QueryString queryString;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Bool bool;

    public Map<String, String> getMatchAll() {
        return matchAll;
    }

    public void setMatchAll(Map<String, String> matchAll) {
        this.matchAll = matchAll;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Map<String, String> getTerm() {
        return term;
    }

    public void setTerm(Map<String, String> term) {
        this.term = term;
    }

    public QueryString getQueryString() {
        return queryString;
    }

    public void setQueryString(QueryString queryString) {
        this.queryString = queryString;
    }

    public Bool getBool() {
        return bool;
    }

    public void setBool(Bool bool) {
        this.bool = bool;
    }
}

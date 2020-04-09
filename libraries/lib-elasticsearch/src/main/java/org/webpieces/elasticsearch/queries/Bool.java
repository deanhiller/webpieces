package org.webpieces.elasticsearch.queries;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class Bool {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Must must;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Filter filter;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Bool bool;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Term> should;

    public Must getMust() {
        return must;
    }

    public void setMust(Must must) {
        this.must = must;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Bool getBool() {
        return bool;
    }

    public void setBool(Bool bool) {
        this.bool = bool;
    }

    public List<Term> getShould() {
        return should;
    }

    public void setShould(List<Term> should) {
        this.should = should;
    }
}


package org.webpieces.elasticsearch.queries;

import java.util.List;

public class QueryString {
    private String query;

    private List<String> fields;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}

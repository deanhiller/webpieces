package org.webpieces.elasticsearch.queries;

import java.util.List;

public class Match {
    private String message;

    private List<String> fields;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }
}

package org.webpieces.elasticsearch.mapping;

import java.util.List;
import java.util.Map;

public class Normalizer {
    private String type;

    private List<String> filter;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }
}

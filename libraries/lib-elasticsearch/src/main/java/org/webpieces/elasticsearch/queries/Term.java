package org.webpieces.elasticsearch.queries;

import java.util.HashMap;
import java.util.Map;

public class Term {

    private Map<String, String> terms = new HashMap<String, String>();

    public Map<String, String> getTerms() {
        return terms;
    }

    public void addTerm(String key, String value) {
        terms.put(key, value);
    }

    public void removeTerm(String key) {
        this.terms.remove(key);
    }
}

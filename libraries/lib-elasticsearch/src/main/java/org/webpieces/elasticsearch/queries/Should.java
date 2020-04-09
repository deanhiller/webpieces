package org.webpieces.elasticsearch.queries;

import java.util.List;

public class Should {
    private List<Term> term;

    public List<Term> getTerm() {
        return term;
    }

    public void setTerm(List<Term> term) {
        this.term = term;
    }
}

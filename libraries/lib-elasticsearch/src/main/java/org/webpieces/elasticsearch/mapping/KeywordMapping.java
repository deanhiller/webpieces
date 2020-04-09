package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KeywordMapping extends AbstractMapping implements PropertyMapping {

    private String type = "keyword";

    private String normalizer = "lowercaseMap";

    @JsonProperty("doc_values")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String docValues;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDocValues() {
        return docValues;
    }

    public void setDocValues(String docValues) {
        this.docValues = docValues;
    }

    public String getNormalizer() {
        return normalizer;
    }

    public void setNormalizer(String normalizer) {
        this.normalizer = normalizer;
    }
}

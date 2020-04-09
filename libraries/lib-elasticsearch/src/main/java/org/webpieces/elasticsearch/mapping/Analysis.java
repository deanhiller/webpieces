package org.webpieces.elasticsearch.mapping;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public class Analysis {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Normalizer> normalizer;

    public Map<String, Normalizer> getNormalizer() {
        return normalizer;
    }

    public void setNormalizer(Map<String, Normalizer> normalizer) {
        this.normalizer = normalizer;
    }
}

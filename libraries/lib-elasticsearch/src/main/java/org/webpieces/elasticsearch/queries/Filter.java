package org.webpieces.elasticsearch.queries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Filter {

    @JsonProperty("geo_distance")
    private Map<String, String> geoDistance;

    public Map<String, String> getGeoDistance() {
        return geoDistance;
    }

    public void setGeoDistance(Map<String, String> geoDistance) {
        this.geoDistance = geoDistance;
    }
}

package org.webpieces.elasticsearch.queries;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoDistance {
    private String distance;
    @JsonProperty("pin.location")
    private String pinLocation;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPinLocation() {
        return pinLocation;
    }

    public void setPinLocation(String pinLocation) {
        this.pinLocation = pinLocation;
    }
}

package com.webpieces.http2parser.api.dto;

public class Http2Priority extends Http2Frame implements HasPriorityDetails {
    public Http2FrameType getFrameType() {
        return Http2FrameType.PRIORITY;
    }

    /* flags */

    /* payload */
    private PriorityDetails priorityDetails = new PriorityDetails();

    public boolean isStreamDependencyIsExclusive() {
        return priorityDetails.streamDependencyIsExclusive;
    }

    public void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive) {
        this.priorityDetails.streamDependencyIsExclusive = streamDependencyIsExclusive;
    }

    public int getStreamDependency() {
        return priorityDetails.streamDependency;
    }

    public void setStreamDependency(int streamDependency) {
        this.priorityDetails.streamDependency = streamDependency & 0x7FFFFFFF;
    }

    public short getWeight() {
        return priorityDetails.weight;
    }

    public void setWeight(short weight) {
        this.priorityDetails.weight = weight;
    }

    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    @Override
    public String toString() {
        return "Http2Priority{" +
                "priorityDetails=" + priorityDetails +
                "} " + super.toString();
    }

    public void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }

}

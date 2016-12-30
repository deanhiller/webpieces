package com.webpieces.http2parser.api.dto;

public class Http2Priority extends AbstractHttp2Frame implements HasPriorityDetails {
    @Override
    public Http2FrameType getFrameType() {
        return Http2FrameType.PRIORITY;
    }

    /* flags */

    /* payload */
    private PriorityDetails priorityDetails = new PriorityDetails();

    @Override
    public boolean isStreamDependencyIsExclusive() {
        return priorityDetails.streamDependencyIsExclusive;
    }

    @Override
    public void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive) {
        this.priorityDetails.streamDependencyIsExclusive = streamDependencyIsExclusive;
    }

    @Override
    public int getStreamDependency() {
        return priorityDetails.streamDependency;
    }

    @Override
    public void setStreamDependency(int streamDependency) {
        this.priorityDetails.streamDependency = streamDependency & 0x7FFFFFFF;
    }

    @Override
    public short getWeight() {
        return priorityDetails.weight;
    }

    @Override
    public void setWeight(short weight) {
        this.priorityDetails.weight = weight;
    }

    @Override
    public PriorityDetails getPriorityDetails() {
        return priorityDetails;
    }

    @Override
    public String toString() {
        return "Http2Priority{" +
                "priorityDetails=" + priorityDetails +
                "} " + super.toString();
    }

    @Override
    public void setPriorityDetails(PriorityDetails priorityDetails) {
        this.priorityDetails = priorityDetails;
    }

}

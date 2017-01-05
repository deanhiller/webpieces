package com.webpieces.http2parser.api.dto.lib;

public class PriorityDetails {
    private boolean streamDependencyIsExclusive = false; //1 bit
    private int streamDependency = 0x0; //31 bits
    private short weight = 0x0; //8

    public boolean isStreamDependencyIsExclusive() {
        return streamDependencyIsExclusive;
    }

    public int getStreamDependency() {
        return streamDependency;
    }

    public short getWeight() {
        return weight;
    }

    public void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive) {
		this.streamDependencyIsExclusive = streamDependencyIsExclusive;
	}

	public void setStreamDependency(int streamDependency) {
		this.streamDependency = streamDependency;
	}

	public void setWeight(short weight) {
		this.weight = weight;
	}

	@Override
    public String toString() {
        return "PriorityDetails{" +
                "streamDependencyIsExclusive=" + streamDependencyIsExclusive +
                ", streamDependency=" + streamDependency +
                ", weight=" + weight +
                '}';
    }
}
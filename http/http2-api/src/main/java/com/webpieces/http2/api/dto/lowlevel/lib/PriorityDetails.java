package com.webpieces.http2.api.dto.lowlevel.lib;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + streamDependency;
		result = prime * result + (streamDependencyIsExclusive ? 1231 : 1237);
		result = prime * result + weight;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PriorityDetails other = (PriorityDetails) obj;
		if (streamDependency != other.streamDependency)
			return false;
		if (streamDependencyIsExclusive != other.streamDependencyIsExclusive)
			return false;
		if (weight != other.weight)
			return false;
		return true;
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
package com.webpieces.http2parser.api.dto;

public interface HasPriorityDetails {
    class PriorityDetails {
        boolean streamDependencyIsExclusive = false; //1 bit
        int streamDependency = 0x0; //31 bits
        short weight = 0x0; //8

        public boolean isStreamDependencyIsExclusive() {
            return streamDependencyIsExclusive;
        }

        public int getStreamDependency() {
            return streamDependency;
        }

        public short getWeight() {
            return weight;
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

    boolean isStreamDependencyIsExclusive();

    void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive);
    int getStreamDependency();

    void setStreamDependency(int streamDependency);

    short getWeight();

    void setWeight(short weight);

    PriorityDetails getPriorityDetails();
    void setPriorityDetails(PriorityDetails priorityDetails);
}

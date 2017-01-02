package com.webpieces.http2parser.api.dto.lib;

@Deprecated
public interface HasPriorityDetails {
    class PriorityDetails {
        public boolean streamDependencyIsExclusive = false; //1 bit
        public int streamDependency = 0x0; //31 bits
        public short weight = 0x0; //8

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

    @Deprecated
    boolean isStreamDependencyIsExclusive();

    @Deprecated
    void setStreamDependencyIsExclusive(boolean streamDependencyIsExclusive);
    @Deprecated
    int getStreamDependency();
    @Deprecated
    void setStreamDependency(int streamDependency);
    @Deprecated
    short getWeight();
    @Deprecated
    void setWeight(short weight);
    @Deprecated
    PriorityDetails getPriorityDetails();
    @Deprecated
    void setPriorityDetails(PriorityDetails priorityDetails);
}

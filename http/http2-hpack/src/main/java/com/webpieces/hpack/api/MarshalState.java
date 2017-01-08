package com.webpieces.hpack.api;

public interface MarshalState {

    void setOutgoingMaxTableSize(int newSize);
    void setOutoingMaxFrameSize(long maxFrameSize);

}

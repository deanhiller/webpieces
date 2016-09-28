package com.webpieces.http2parser.api;

import com.webpieces.http2parser.impl.ParserResultImpl;
import org.webpieces.data.api.DataWrapper;

import java.util.ArrayList;
import java.util.List;

public interface Http2Frame {
    static ParserResult parse(DataWrapper oldData, DataWrapper newData) {
        DataWrapper wrapperToParse;
        List<Http2Frame> frames = new ArrayList<>();

        if (oldData.getReadableSize() > 0)
            wrapperToParse = Http2FrameUtil.dataGen.chainDataWrappers(oldData, newData);
        else
            wrapperToParse = newData;

        // Loop until a return (ack)
        while (true) {
            int lengthOfData = wrapperToParse.getReadableSize();
            if (lengthOfData <= 3) {
                // Not even a length
                return new ParserResultImpl(frames, wrapperToParse);
            } else {
                // peek for length
                int length = Http2FrameUtil.peekLengthOfFrame(wrapperToParse);
                if (lengthOfData < length) {
                    // not a whole frame
                    return new ParserResultImpl(frames, wrapperToParse);
                } else {
                    // parse a single frame, look for more
                    List<? extends DataWrapper> split = Http2FrameUtil.dataGen.split(wrapperToParse, length);
                    Http2Frame frame = Http2FrameUtil.getFromDataWrapper(split.get(0));
                    frames.add(frame);
                    wrapperToParse = split.get(1);
                }
            }
        }
    }

    void setStreamId(int streamId);

    int getStreamId();

    Http2FrameType getFrameType();

    DataWrapper getDataWrapper();

    byte[] getBytes();

    // These should not actually be public parts of the interface but they are needed
    // for the subclasses etc.
    byte getFlagsByte();

    void setFlags(byte flag);

    void setPayloadFromDataWrapper(DataWrapper data);

    DataWrapper getPayloadDataWrapper();

}

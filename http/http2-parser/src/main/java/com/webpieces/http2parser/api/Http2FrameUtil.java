package com.webpieces.http2parser.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;

import java.nio.ByteBuffer;
import java.util.List;

class Http2FrameUtil {
    static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    // includes header length
    static int peekLengthOfFrame(DataWrapper data) {
        ByteBuffer lengthBytes = ByteBuffer.wrap(data.readBytesAt(0, 3));
        int length = lengthBytes.getShort() << 8;
        length |= lengthBytes.get();
        return length + 9; // add 9 bytes for the header
    }

    // Ignores what's left over at the end of the datawrapper
    static Http2Frame getFromDataWrapper(DataWrapper data) {
        ByteBuffer headerByteBuffer = ByteBuffer.wrap(data.readBytesAt(0, 9));
        int length = headerByteBuffer.getShort() << 8;
        length |= headerByteBuffer.get();

        byte frameTypeId = headerByteBuffer.get();

        Class<? extends Http2Frame> frameClass = Http2FrameType.fromId(frameTypeId).getFrameClass();
        try {
            Http2Frame ret = frameClass.newInstance();

            byte flags = headerByteBuffer.get();
            ret.setFlags(flags);

            // Ignore the reserved bit
            int streamId = headerByteBuffer.getInt();
            ret.setStreamId(streamId);

            if(length > 0) {
                List<? extends DataWrapper> splitWrappers = dataGen.split(data, 9);
                DataWrapper payloadPlusMore = splitWrappers.get(1);
                List<? extends DataWrapper> split = dataGen.split(payloadPlusMore, length);
                ret.setPayloadFromDataWrapper(split.get(0));
            }
            return ret;

        } catch (InstantiationException | IllegalAccessException e) {
            // TODO: deal with exception
            return null; // should reraise in some fashion
        }
    }
}

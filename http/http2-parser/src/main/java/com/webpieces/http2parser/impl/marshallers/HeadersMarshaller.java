package com.webpieces.http2parser.impl.marshallers;

import java.nio.ByteBuffer;
import java.util.List;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

import com.webpieces.http2parser.api.dto.HeadersFrame;
import com.webpieces.http2parser.api.dto.error.ConnectionException;
import com.webpieces.http2parser.api.dto.error.CancelReasonCode;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.PriorityDetails;
import com.webpieces.http2parser.impl.DataSplit;
import com.webpieces.http2parser.impl.Http2MementoImpl;
import com.webpieces.http2parser.impl.PaddingUtil;

public class HeadersMarshaller extends AbstractFrameMarshaller implements FrameMarshaller {

    public HeadersMarshaller(BufferPool bufferPool, DataWrapperGenerator dataGen) {
        super(bufferPool);
    }

	@Override
	public DataWrapper marshal(Http2Frame frame) {
        HeadersFrame castFrame = (HeadersFrame) frame;
        if(frame.getStreamId() == 0)
        	throw new ConnectionException(CancelReasonCode.INVALID_STREAM_ID, frame.getStreamId(), "Headers frame cannot be streamId 0");
        
        int paddingSize = castFrame.getPadding().getReadableSize();

        byte value = 0x0;
        if (castFrame.isEndOfStream()) value |= 0x1;
        if (castFrame.isEndHeaders()) value |= 0x4;
        if (paddingSize > 0) value |= 0x8;
        if (castFrame.isPriority()) value |= 0x20;
        
        DataWrapper preludeDW;
        PriorityDetails priorityDetails = castFrame.getPriorityDetails();
        if(priorityDetails != null) {
        	preludeDW = PriorityMarshaller.marshalPriorityDetails(bufferPool, priorityDetails, frame);
        } else {
            preludeDW = dataGen.emptyWrapper();
        }

        DataWrapper unpadded = dataGen.chainDataWrappers(
                preludeDW,
                castFrame.getHeaderFragment());
        
        DataWrapper payload = PaddingUtil.padDataIfNeeded(unpadded, castFrame.getPadding());
        return super.marshalFrame(frame, value, payload);
	}

	@Override
	public AbstractHttp2Frame unmarshal(Http2MementoImpl state, DataWrapper framePayloadData) {
        HeadersFrame frame = new HeadersFrame();
        super.unmarshalFrame(state, frame);

        byte flagsByte = state.getFrameHeaderData().getFlagsByte();
        frame.setEndOfStream((flagsByte & 0x1) == 0x1);
        frame.setEndHeaders((flagsByte & 0x4) == 0x4);
        boolean isPadded = (flagsByte & 0x8) == 0x8;
        
        PriorityDetails priorityDetails = null;
        if((flagsByte & 0x20) == 0x20) {
        	priorityDetails = new PriorityDetails();
        	frame.setPriorityDetails(priorityDetails);
        }

        DataSplit padSplit = PaddingUtil.extractPayloadAndPadding(isPadded, framePayloadData, frame.getStreamId());
        frame.setPadding(padSplit.getPadding());
        DataWrapper paddingStripped = padSplit.getPayload();

        if(priorityDetails != null) {
        	//1 bit Exclusive flag, 31 bits stream dependency, and 8 bits weight = 5 bytes....
            List<? extends DataWrapper> split = dataGen.split(paddingStripped, 5);
            ByteBuffer preludeBytes = bufferPool.createWithDataWrapper(split.get(0));

            int firstInt = preludeBytes.getInt();
            priorityDetails.setStreamDependencyIsExclusive((firstInt >>> 31) == 0x1);
            int streamDependency = firstInt & 0x7FFFFFFF;
            if(streamDependency == frame.getStreamId()) {
                // Can't depend on self
                throw new ConnectionException(CancelReasonCode.BAD_STREAM_DEPENDENCY, streamDependency, 
                		"stream id="+streamDependency+" depends on itself");
            }
            priorityDetails.setStreamDependency(streamDependency);
            priorityDetails.setWeight((short) (preludeBytes.get() & 0xFF));
            frame.setHeaderFragment(split.get(1));
            bufferPool.releaseBuffer(preludeBytes);
        } else {
            frame.setHeaderFragment(paddingStripped);
        }
        
        if(frame.getStreamId() == 0)
            throw new ConnectionException(CancelReasonCode.INVALID_STREAM_ID, frame.getStreamId(), 
            		"headers frame had invalid stream id="+frame.getStreamId());        
        
        return frame;
	}
}

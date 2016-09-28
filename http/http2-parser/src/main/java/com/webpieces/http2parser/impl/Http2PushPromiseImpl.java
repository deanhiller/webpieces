package com.webpieces.http2parser.impl;

import com.webpieces.http2parser.api.Http2FrameType;
import com.webpieces.http2parser.api.Http2Padded;
import com.webpieces.http2parser.api.Http2PushPromise;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.impl.ByteBufferDataWrapper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Http2PushPromiseImpl extends Http2FrameImpl implements Http2PushPromise {

	public Http2FrameType getFrameType() {
		return Http2FrameType.PUSH_PROMISE;
	}

	/* flags */
	private boolean endHeaders; /* 0x4 */
	private boolean padded; /* 0x8 */

	public boolean isEndHeaders() {
		return endHeaders;
	}

	public void setEndHeaders() {
		this.endHeaders = true;
	}

	public byte getFlagsByte() {
		byte value = 0x0;
		if(endHeaders) value |= 0x4;
		if(padded) value |= 0x8;
		return value;
	}
	public void setFlags(byte flags) {
		endHeaders = (flags & 0x4) == 0x4;
		padded = (flags & 0x8) == 0x8;
	}

    /* payload */
	// reserved - 1bit
	private int promisedStreamId; //31bits
	private Http2HeaderBlock headerBlock;
	private byte[] padding;

	public void setPadding(byte[] padding) {
		this.padding = padding;
		padded = true;
	}

    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId;
    }

	// Should reuse code in Http2HeadersImpl but multiple-inheritance is not possible?
	public void setHeaders(Map<String, String> headers) {
		List<Http2HeaderBlock.Header> headerList = new ArrayList<>();
		for(Map.Entry<String, String> entry: headers.entrySet()) {
			headerList.add(new Http2HeaderBlock.Header(entry.getKey(), entry.getValue()));
		}
		headerBlock = new Http2HeaderBlock(headerList);
	}

	public Map<String, String> getHeaders() {
		return headerBlock.toMap();
	}

	public DataWrapper getPayloadDataWrapper() {
		ByteBuffer prelude = ByteBuffer.allocate(4);
		prelude.putInt(promisedStreamId);
		prelude.flip();

		DataWrapper headersDW = headerBlock.getDataWrapper();
		DataWrapper finalDW = dataGen.chainDataWrappers(
				new ByteBufferDataWrapper(prelude),
				headersDW);
		if(padded)
			return finalDW;
		else
			return Http2Padded.pad(padding, finalDW);
	}

	public void setPayloadFromDataWrapper(DataWrapper payload) {
		List<? extends DataWrapper> split = dataGen.split(payload, 4);
		ByteBuffer prelude = ByteBuffer.wrap(split.get(0).createByteArray());
		promisedStreamId = prelude.getInt() & 0x7FFFFFFF;

		// TODO: Reuse this code among the frames which need to deal with padding
		if(padded) {
			byte padLength = split.get(1).readByteAt(0);
			List<? extends DataWrapper> split1 = dataGen.split(split.get(1), 1);
			List<? extends DataWrapper> split2 = dataGen.split(split1.get(1), payload.getReadableSize() - padLength);
			headerBlock = new Http2HeaderBlock(split2.get(0));
			padding = split2.get(1).createByteArray();
		} else {
			padding = new byte[0];
			headerBlock = new Http2HeaderBlock(split.get(1));
		}
	}
}

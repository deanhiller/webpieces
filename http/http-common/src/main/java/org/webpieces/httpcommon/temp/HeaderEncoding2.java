package org.webpieces.httpcommon.temp;

import java.util.LinkedList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.impl.HeaderEncoding;
import com.webpieces.http2parser.api.dto.PushPromiseFrame;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class HeaderEncoding2 {

	private HeaderEncoding encoding = new HeaderEncoding();
	private Encoder encoder;
	private int maxFrameSize;
	
	public HeaderEncoding2(Encoder encoder, int maxFrameSize) {
		this.encoder = encoder;
		this.maxFrameSize = maxFrameSize;
	}
	
	public List<Http2Frame> createPushPromises(LinkedList<Http2Header> headers, int streamId, int promisedStreamId) {
		PushPromiseFrame promise = new PushPromiseFrame();
		promise.setStreamId(streamId);
		promise.setPromisedStreamId(promisedStreamId);
		
		return createHeaderFrames(promise, headers);
	}

	public List<Http2Frame> createHeaderFrames(HasHeaderFragment frame, LinkedList<Http2Header> headerList) {
		return encoding.createHeaderFrames(frame, headerList, encoder, maxFrameSize);
	}

	public void setMaxFrameSize(int maxValue) {
		this.maxFrameSize = maxValue;
	}

	public DataWrapper serializeHeaders(LinkedList<Http2Header> headers) {
		return encoding.serializeHeaders(encoder, headers);
	}

	
}

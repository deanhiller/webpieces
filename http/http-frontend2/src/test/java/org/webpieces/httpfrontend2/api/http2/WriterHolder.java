package org.webpieces.httpfrontend2.api.http2;

import org.webpieces.util.futures.XFuture;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.StreamWriter;

public class WriterHolder {

	private StreamWriter writer1;
	private XFuture<StreamWriter> future2;
	private Http2Response resp1;
	private Http2Response resp2;

	public WriterHolder(StreamWriter writer1, XFuture<StreamWriter> future2, Http2Response resp12, Http2Response resp22) {
		this.writer1 = writer1;
		this.future2 = future2;
		this.resp1 = resp12;
		this.resp2 = resp22;
	}

	public StreamWriter getWriter1() {
		return writer1;
	}

	public XFuture<StreamWriter> getFuture2() {
		return future2;
	}

	public Http2Response getResp1() {
		return resp1;
	}

	public Http2Response getResp2() {
		return resp2;
	}

	
}

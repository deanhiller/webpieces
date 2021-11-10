package org.webpieces.router.api.error;

import java.util.Map;
import org.webpieces.util.futures.XFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.RouterResponseHandler;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

public class MockStreamHandle implements RouterResponseHandler {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private Http2Response lastResponse;
	private DataWrapper allData = dataGen.emptyWrapper();

	private boolean tooManyResponses;

	private boolean wasClosed;

	@Override
	public XFuture<StreamWriter> process(Http2Response response) {
		if(this.lastResponse != null)
			tooManyResponses = true;
		this.lastResponse = response;
		
		return XFuture.completedFuture(new MockStreamWriter());
	}
	
	private class MockStreamWriter implements StreamWriter {

		@Override
		public XFuture<Void> processPiece(StreamMsg data) {
			DataFrame frame = (DataFrame) data;
			allData = dataGen.chainDataWrappers(allData, frame.getData());
			
			return XFuture.completedFuture(null);
		}
		
	}
	
	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public XFuture<Void> cancel(CancelReason payload) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getSocket() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean requestCameFromHttpsSocket() {
		return false;
	}

	@Override
	public boolean requestCameFromBackendSocket() {
		return false;
	}

	@Override
	public Void closeIfNeeded() {
		wasClosed = true;
		return null;
	}

	public Http2Response getLastResponse() {
		if(tooManyResponses)
			throw new IllegalStateException("There was too many responses.  this mock only can take one response at a time");
		Http2Response temp = lastResponse;
		lastResponse = null;
		return temp;
	}

	public String getResponseBody() {
		String content = allData.createStringFromUtf8(0, allData.getReadableSize());
		allData = dataGen.emptyWrapper();
		return content;
	}

	public boolean isWasClosed() {
		return wasClosed;
	}

	@Override
	public void closeSocket(String reason) {
		throw new UnsupportedOperationException("not supported yet");
	}

	
}

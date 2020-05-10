package org.webpieces.router.api.error;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.router.api.RouterStreamHandle;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public class MockStreamHandle implements RouterStreamHandle {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private Http2Response response;
	private DataWrapper allData = dataGen.emptyWrapper();

	@Override
	public CompletableFuture<StreamWriter> process(Http2Response response) {
		this.response = response;
		return CompletableFuture.completedFuture(new MockStreamWriter());
	}

	private class MockStreamWriter implements StreamWriter {

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			DataFrame frame = (DataFrame) data;
			allData = dataGen.chainDataWrappers(allData, frame.getData());
			
			return CompletableFuture.completedFuture(null);
		}
		
	}
	
	@Override
	public PushStreamHandle openPushStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CompletableFuture<Void> cancel(CancelReason payload) {
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
		throw new UnsupportedOperationException();
	}

	public Http2Response getResponse() {
		return response;
	}

	public String getResponseBody() {
		String content = allData.createStringFromUtf8(0, allData.getReadableSize());
		return content;
	}

}

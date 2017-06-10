package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient.api.DataWriter;
import org.webpieces.httpclient.api.HttpFullResponse;
import org.webpieces.httpclient.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class CompletableListener implements HttpResponseListener {

	private final static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private CompletableFuture<HttpFullResponse> future;
	private HttpFullResponse response;

	public CompletableListener(CompletableFuture<HttpFullResponse> future) {
		this.future = future;
	}

	@Override
	public CompletableFuture<DataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		HttpFullResponse resp1 = new HttpFullResponse(resp, dataGen.emptyWrapper());

		if(isComplete) {
			future.complete(resp1);
			return CompletableFuture.completedFuture(new NullWriter());
		}
		
		response = resp1;
		return CompletableFuture.completedFuture(new DataWriterImpl());
	}

	private class NullWriter implements DataWriter {
		@Override
		public CompletableFuture<DataWriter> incomingData(HttpData data) {
			throw new UnsupportedOperationException("This should not happen");
		}
	}
	
	private class DataWriterImpl implements DataWriter {
		@Override
		public CompletableFuture<DataWriter> incomingData(HttpData chunk) {
			DataWrapper allData = dataGen.chainDataWrappers(response.getData(), chunk.getBodyNonNull());
			response.setData(allData);
			
			if(chunk.isEndOfData()) {
				future.complete(response);
				response = null;
			}
			
			return CompletableFuture.completedFuture(this);
		}
	}
	
	@Override
	public void failure(Throwable e) {
		future.completeExceptionally(e);
	}

}

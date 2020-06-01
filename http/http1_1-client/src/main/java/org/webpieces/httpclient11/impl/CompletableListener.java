package org.webpieces.httpclient11.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpFullResponse;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpStreamRef;
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
	public HttpStreamRef incomingResponse(HttpResponse resp, boolean isComplete) {
		HttpFullResponse resp1 = new HttpFullResponse(resp, dataGen.emptyWrapper());

		if(isComplete) {
			future.complete(resp1);
			CompletableFuture<HttpDataWriter> f = CompletableFuture.completedFuture(new NullWriter());
			return new ProxyStreamRef(null, f);
		}
		
		response = resp1;

		CompletableFuture<HttpDataWriter> f = CompletableFuture.completedFuture(new DataWriterImpl());
		return new ProxyStreamRef(null, f);
	}

	private class NullWriter implements HttpDataWriter {
		@Override
		public CompletableFuture<Void> send(HttpData data) {
			throw new UnsupportedOperationException("This should not happen");
		}
	}
	
	private class DataWriterImpl implements HttpDataWriter {
		@Override
		public CompletableFuture<Void> send(HttpData chunk) {
			DataWrapper allData = dataGen.chainDataWrappers(response.getData(), chunk.getBodyNonNull());
			response.setData(allData);
			
			if(chunk.isEndOfData()) {
				future.complete(response);
				response = null;
			}
			
			return CompletableFuture.completedFuture(null);
		}
	}
	
	@Override
	public void failure(Throwable e) {
		future.completeExceptionally(e);
	}

}

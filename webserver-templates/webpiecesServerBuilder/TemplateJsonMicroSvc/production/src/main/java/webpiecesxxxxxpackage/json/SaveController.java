package webpiecesxxxxxpackage.json;

import org.webpieces.plugin.json.Jackson;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import webpiecesxxxxxpackage.deleteme.api.SaveApi;
import webpiecesxxxxxpackage.deleteme.api.SearchRequest;
import webpiecesxxxxxpackage.deleteme.api.SearchResponse;
import webpiecesxxxxxpackage.deleteme.remoteapi.FetchValueRequest;
import webpiecesxxxxxpackage.deleteme.remoteapi.FetchValueResponse;
import webpiecesxxxxxpackage.deleteme.remoteapi.RemoteApi;

@Singleton
public class SaveController implements SaveApi {
	
	private static final Logger log = LoggerFactory.getLogger(SaveController.class);

	private Counter counter;
	private RemoteApi remoteService;

	@Inject
	public SaveController(MeterRegistry metrics, RemoteApi remoteService) {
		counter = metrics.counter("testCounter");
		this.remoteService = remoteService;
	}


	@Override
	public XFuture<SearchResponse> search(@Jackson SearchRequest request) {

		FetchValueRequest fetchReq = new FetchValueRequest();
		fetchReq.setName(request.getQuery());
		XFuture<FetchValueResponse> future = remoteService.fetchValue(fetchReq);

		XFuture<SearchResponse> searchFuture = future.thenApply((resp) -> {
			SearchResponse r = new SearchResponse();
			r.setSearchTime(5);
			return r;
		});

		return searchFuture;
	}
}

package webpiecesxxxxxpackage.json;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.futures.XFuture;
import webpiecesxxxxxpackage.service.RemoteService;
import webpiecesxxxxxpackage.service.SendDataRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SearchController implements SearchApi {

	private static final Logger log = LoggerFactory.getLogger(SearchController.class);

	private Counter counter;
	private RemoteService remoteService;

	@Inject
	public SearchController(MeterRegistry metrics, RemoteService remoteService) {
		counter = metrics.counter("testCounter");
		this.remoteService = remoteService;
	}

	@Override
	public XFuture<SearchResponse> search(SearchRequest request) {
		counter.increment();

		//so we can test out mocking remote services
		remoteService.sendData(new SendDataRequest(6)).join();
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(99);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		return XFuture.completedFuture(resp);
	}

}

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
import webpiecesxxxxxpackage.deleteme.api.SaveRequest;
import webpiecesxxxxxpackage.deleteme.api.SaveResponse;
import webpiecesxxxxxpackage.deleteme.api.TheMatch;
import webpiecesxxxxxpackage.deleteme.remoteapi.*;

import java.util.ArrayList;
import java.util.List;

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
	public XFuture<SaveResponse> save(@Jackson SaveRequest request) {

		FetchValueRequest fetchReq = new FetchValueRequest();

		List<MyEnum> enumList = new ArrayList<>();
		enumList.add(MyEnum.DEAN);
		enumList.add(MyEnum.DEAN);
		fetchReq.setTestEnumList(enumList);
		fetchReq.setName(request.getQuery());
		MyThing thing = new MyThing();
		thing.setThingsName("deano");
		fetchReq.setThing(thing);
		XFuture<FetchValueResponse> future = remoteService.fetchValue(fetchReq);

		XFuture<SaveResponse> searchFuture = future.thenApply((resp) -> {
			SaveResponse r = new SaveResponse();
			r.setSuccess(true);
			r.setSearchTime(5);
			TheMatch match1 = new TheMatch();
			match1.setMatching("dean");
			TheMatch match2 = new TheMatch();
			match2.setMatching("joe");
			List<TheMatch> list = new ArrayList<>();
			list.add(match1);
			list.add(match2);
			r.setMatches(list);
			return r;
		});

		return searchFuture;
	}
}

package webpiecesxxxxxpackage.json;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.DataFrame;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.plugin.json.Jackson;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.util.futures.XFuture;
import webpiecesxxxxxpackage.service.RemoteService;
import webpiecesxxxxxpackage.service.SendDataRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class SaveController implements SaveApi {

	private static final Logger log = LoggerFactory.getLogger(SaveController.class);

	private Counter counter;
	private RemoteService remoteService;

	@Inject
	public SaveController(MeterRegistry metrics, RemoteService remoteService) {
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

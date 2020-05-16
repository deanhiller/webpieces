package webpiecesxxxxxpackage.json;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.json.Jackson;
import org.webpieces.router.api.exceptions.AuthorizationException;
import org.webpieces.router.api.exceptions.NotFoundException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import webpiecesxxxxxpackage.web.login.AppLoginController;

@Singleton
public class JsonController {
	
	private static final Logger log = LoggerFactory.getLogger(JsonController.class);

	private Counter counter;

	@Inject
	public JsonController(MeterRegistry metrics) {
		counter = metrics.counter("testCounter");
	}
	
	public CompletableFuture<SearchResponse> asyncJsonRequest(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(8);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return CompletableFuture.completedFuture(resp);
	}
	
	public SearchResponse jsonRequest(int id, @Jackson SearchRequest request) {
		counter.increment();
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(5);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	public SearchResponse postJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(99);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return resp;
	}
	
	public CompletableFuture<UploadResponse> postFileUpload(@Jackson FileUploadPiece piece) {
		String token = Current.session().getOrCreateSecureToken();
	
		String user = Current.session().get(AppLoginController.TOKEN);
		if(user == null)
			throw new AuthorizationException("Not logged in");
		else if(!token.equals(piece.getSecureToken()))
			throw new AuthorizationException("CSRF prevention kicked in");
	
		log.info("piece="+piece.getFileName()+" pos="+piece.getPosition()+" size="+piece.getSliceSize());
		return CompletableFuture.completedFuture(new UploadResponse(true));
		
	}
	
	public CompletableFuture<SearchResponse> postAsyncJson(int id, @Jackson SearchRequest request) {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(98);
		resp.getMatches().add("match1");
		resp.getMatches().add("match2");
		
		return CompletableFuture.completedFuture(resp);
	}
	
	@Jackson
	public SearchResponse readOnly() {
		SearchResponse resp = new SearchResponse();
		resp.setSearchTime(1);
		return resp;
	}
	
	public SearchResponse throwNotFound(int id, @Jackson SearchRequest request) {
		throw new NotFoundException("to test it out");
	}
}

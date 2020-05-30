package webpiecesxxxxxpackage.web.main;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.router.api.RouterStreamHandle;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

import webpiecesxxxxxpackage.base.GlobalAppContext;
import webpiecesxxxxxpackage.mgmt.SomeBean;
import webpiecesxxxxxpackage.service.RemoteService;
import webpiecesxxxxxpackage.service.SomeLibrary;

@Singleton
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	private final RemoteService service;
	private final SomeLibrary someLib;
	
	//This is injected to demonstrate the properties plugin so you can modify properties via a web page and changes are stored in database
	//so changes will survive a restart.
	private final SomeBean managed;
	private GlobalAppContext injectedCtx;

	@Inject
	public MainController(RemoteService service, SomeLibrary someLib, SomeBean managed, GlobalAppContext injectedCtx) {
		super();
		this.service = service;
		this.someLib = someLib;
		this.managed = managed;
		this.injectedCtx = injectedCtx;
	}

	public Action index() {
		//this is so the test can throw an exception from some random library that is mocked
		someLib.doSomething(5); 
		
		//renderThis renders index.html in the same package as this controller class
		return Actions.renderThis(); 
	}

	public Action mySyncMethod() {
		
		GlobalAppContext ctx = (GlobalAppContext) Current.applicationContext();
		
		if(ctx != injectedCtx)
			throw new RuntimeException("We should fail here");
		
		return Actions.renderThis("value", 21);
	}
	
	public CompletableFuture<Action> myAsyncMethod() {
		CompletableFuture<Integer> remoteValue = service.fetchRemoteValue("dean", 21);
		return remoteValue.thenApply(s -> convertToAction(s));
	}
	
	//called from method above
	private Action convertToAction(int value) {
		return Actions.renderThis("value", value);
	}
	
	public Render notFound() {
		return Actions.renderThis();
	}
	
	public Render internalError() {		
		Current.flash().clear();
		Current.validation().clear();
		return Actions.renderThis();
	}

	//Method signature cannot have RequestContext since in microservices, we implement an api as the server
	//AND a client implements the same api AND client does not have a RequestContext!!
	public CompletableFuture<StreamWriter> myStream(RouterStreamHandle handle) {
		RequestContext requestCtx = Current.getContext(); 
		
		Http2Response response = handle.createBaseResponse(requestCtx.getRequest().originalRequest, "text/plain", 200, "Ok");
		response.setEndOfStream(false);
		
		return handle.process(response).thenApply(responseWriter -> new RequestStreamEchoWriter(requestCtx, responseWriter));
	}

	private static class RequestStreamEchoWriter implements StreamWriter {

		private AtomicInteger total = new AtomicInteger();
		private StreamWriter handle;

		public RequestStreamEchoWriter(RequestContext requestCtx, StreamWriter responseWriter) {
			this.handle = responseWriter;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			DataFrame f = (DataFrame) data;
			int numReceived = total.addAndGet(f.getData().getReadableSize());
			log.info("Num bytes received so far="+numReceived);
			
			if(data.isEndOfStream()) {
				log.info("Upload complete");
				//usually you may do something different here at end of stream
				return handle.processPiece(data);
			}

			//We just echo data back to whatever the client sent as the client sends it...
			return handle.processPiece(data);
		}
	}

}

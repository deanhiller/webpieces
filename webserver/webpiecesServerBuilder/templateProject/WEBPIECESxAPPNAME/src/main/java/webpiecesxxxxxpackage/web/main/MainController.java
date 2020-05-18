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

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.DataFrame;
import com.webpieces.http2parser.api.dto.lib.StreamMsg;

import webpiecesxxxxxpackage.GlobalAppContext;
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
		return Actions.renderThis();
	}

	public CompletableFuture<StreamWriter> myStream(RequestContext requestCtx, RouterStreamHandle handle) {
		return CompletableFuture.completedFuture(new RequestStreamEchoWriter(handle));
	}

	private static class RequestStreamEchoWriter implements StreamWriter {

		private AtomicInteger total = new AtomicInteger();
		private RouterStreamHandle handle;

		public RequestStreamEchoWriter(RouterStreamHandle handle) {
			this.handle = handle;
		}

		@Override
		public CompletableFuture<Void> processPiece(StreamMsg data) {
			
			if(data.isEndOfStream()) {
				log.info("Upload complete");
				return handle.createFullRedirect(MainRouteId.MAIN_ROUTE);
			}
			
			DataFrame frame = (DataFrame) data;
			int totalSoFar = total.addAndGet(frame.getData().getReadableSize());

			//log.info("STEP 2. returning echo future for bytes="+frame.getData().getReadableSize()+" eos="+frame.isEndOfStream()+" totalSoFar="+totalSoFar);
			return CompletableFuture.completedFuture(null);
		}
	}

//	public CompletableFuture<StreamWriter> myStream(RequestContext requestCtx, RouterStreamHandle handle) {
//
//
//		return CompletableFuture.<StreamWriter>completedFuture(new RequestStreamEchoWriter(requestCtx, handle));
//	}
//
//	private static class RequestStreamEchoWriter implements StreamWriter {
//
//		private RequestContext requestCtx;
//		private RouterStreamHandle handle;
//
//		public RequestStreamEchoWriter(RequestContext requestCtx, RouterStreamHandle handle) {
//			this.requestCtx = requestCtx;
//			this.handle = handle;
//		}
//
//		@Override
//		public CompletableFuture<Void> processPiece(StreamMsg data) {
//			log.info("streaming in piece");
//
////			try {
////				Thread.sleep(1000);
////			} catch (InterruptedException e) {
////			}
//
//			if(data.isEndOfStream()) {
//				Http2Request req = requestCtx.getRequest().originalRequest;
//				Http2Response redirect = handle.createRedirect(req, new RedirectResponse("/"));
//				return handle.process(redirect).thenApply(s -> null);
//			}
//
//			return CompletableFuture.completedFuture(null);
//		}
//	}
}

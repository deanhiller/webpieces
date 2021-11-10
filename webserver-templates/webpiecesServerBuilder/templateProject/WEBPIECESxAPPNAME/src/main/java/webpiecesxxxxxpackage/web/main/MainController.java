package webpiecesxxxxxpackage.web.main;

import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

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
	private ApplicationContext ctx2;

	@Inject
	public MainController(RemoteService service, SomeLibrary someLib, SomeBean managed, GlobalAppContext injectedCtx, ApplicationContext ctx2) {
		super();
		this.service = service;
		this.someLib = someLib;
		this.managed = managed;
		this.injectedCtx = injectedCtx;
		this.ctx2 = ctx2;
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
		else if(ctx2 != ctx)
			throw new RuntimeException("Bug, ctx should be the same");
		
		return Actions.renderThis("value", 21);
	}
	
	public XFuture<Action> myAsyncMethod() {
		XFuture<Integer> remoteValue = service.fetchRemoteValue("dean", 21);
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

}

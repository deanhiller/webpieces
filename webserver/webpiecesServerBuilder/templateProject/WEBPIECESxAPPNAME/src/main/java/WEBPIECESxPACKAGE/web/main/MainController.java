package WEBPIECESxPACKAGE.web.main;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

import WEBPIECESxPACKAGE.service.RemoteService;
import WEBPIECESxPACKAGE.service.SomeLibrary;
import WEBPIECESxPACKAGE.mgmt.SomeBean;

@Singleton
public class MainController {

	private final RemoteService service;
	private final SomeLibrary someLib;
	
	//This is injected to demonstrate the properties plugin so you can modify properties via a web page and changes are stored in database
	//so changes will survive a restart.
	private final SomeBean managed;

	@Inject
	public MainController(RemoteService service, SomeLibrary someLib, SomeBean managed) {
		super();
		this.service = service;
		this.someLib = someLib;
		this.managed = managed;
	}

	public Action index() {
		//this is so the test can throw an exception from some random library that is mocked
		someLib.doSomething(5); 
		
		//renderThis renders index.html in the same package as this controller class
		return Actions.renderThis(); 
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
}

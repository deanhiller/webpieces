package org.webpieces.webserver.basic.app.biz;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.webserver.async.app.AsyncRouteId;

@Singleton
public class AsyncController {

	private final SomeOtherLib notFoundLib;
	private final SomeLib errorLib;
	
	@Inject
	public AsyncController(SomeOtherLib notFoundLib, SomeLib errorLib) {
		super();
		this.notFoundLib = notFoundLib;
		this.errorLib = errorLib;
	}

	public CompletableFuture<Action> someMethod() {
		return notFoundLib.someBusinessLogic().thenApply(s -> Actions.redirect(AsyncRouteId.SOME_ROUTE));
	}
	
	public CompletableFuture<Action> redirect(String id) {
		Redirect redirect = Actions.redirect(AsyncRouteId.SOME_ROUTE);
		return CompletableFuture.completedFuture(redirect);		
	}

	public CompletableFuture<Action> redirectWithInt(int id) {
		Redirect redirect = Actions.redirect(AsyncRouteId.SOME_ROUTE);
		return CompletableFuture.completedFuture(redirect);
	}

	public CompletableFuture<Action> myMethod() {
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		Render renderThis = Actions.renderThis("hhhh", 86);
		return CompletableFuture.completedFuture(renderThis);
	}
	
	public CompletableFuture<Action> asyncMyMethod() {
		return notFoundLib.someBusinessLogic().thenApply(s -> {
			return Actions.renderView("userParamPage.html", "user", "Dean Hiller");
		});
	}
	
	public CompletableFuture<Action> asyncFail() {
		return notFoundLib.someBusinessLogic().thenApply(s -> {
			return Actions.renderView("userParamPage.html");
		});
	}
	
	public CompletableFuture<Action> throwNotFound() {
		return notFoundLib.someBusinessLogic().thenApply(s -> Actions.redirect(AsyncRouteId.SOME_ROUTE));
	}
	
	public CompletableFuture<Render> notFound() {
		//we use this to mock and throw NotFoundException or some RuntimeException for testing notFound path failures
		return notFoundLib.someBusinessLogic().thenApply(s -> Actions.renderThis());
	}
	
	public CompletableFuture<Render> internalError() {
		//we use this to mock and throw exceptions when needed for testing
		return errorLib.someBusinessLogic().thenApply(s -> Actions.renderThis());
	}
	
}

package org.webpieces.webserver.basic.biz;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.RenderHtml;
import org.webpieces.webserver.async.AsyncRouteId;

public class AsyncController {

	@Inject
	private NotFoundLib notFoundLib;
	@Inject
	private InternalSvrErrorLib errorLib;
	
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
		RenderHtml renderThis = Actions.renderThis("hhhh", 86);
		return CompletableFuture.completedFuture(renderThis);
	}
	
	public CompletableFuture<Action> throwNotFound() {
		return notFoundLib.someBusinessLogic().thenApply(s -> Actions.redirect(AsyncRouteId.SOME_ROUTE));
	}
	
	public CompletableFuture<Action> notFound() {
		//we use this to mock and throw NotFoundException or some RuntimeException for testing notFound path failures
		return notFoundLib.someBusinessLogic().thenApply(s -> Actions.renderThis());
	}
	
	public CompletableFuture<Action> internalError() {
		//we use this to mock and throw exceptions when needed for testing
		return errorLib.someBusinessLogic().thenApply(s -> Actions.renderThis());
	}
	
}

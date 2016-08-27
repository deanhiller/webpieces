package PACKAGE.example;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;

import PACKAGE.CLASSNAMERouteId;

public class CLASSNAMELocalController {

	@Inject
	private RemoteService service;
	
	public Action home() {
		return Actions.renderThis("user", "Dean Hiller");
	}
	
	public CompletableFuture<Action> myAsyncMethod() {
		CompletableFuture<Integer> remoteValue = service.fetchRemoteValue();
		return remoteValue.thenApply(s -> convertToAction(s));
	}
	
	private Action convertToAction(int value) {
		return Actions.renderThis("value", value);
	}
	
	public Action redirect(String id) {
		return Actions.redirect(CLASSNAMERouteId.RENDER_PAGE);
	}
	
	public Action notFound() {
		return Actions.renderThis();
	}
	
	public Action internalError() {
		return Actions.renderThis();
	}
}

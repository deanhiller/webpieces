package WEBPIECESxPACKAGE.example.crud;

import javax.inject.Inject;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;

import WEBPIECESxPACKAGE.ExampleRouteId;
import WEBPIECESxPACKAGE.example.RemoteService;

public class ExampleController {

	@Inject
	private RemoteService service;
	
	public Action userForm() {
		return Actions.renderThis("user", "Dean Hiller");
	}

	public Redirect postUser() {
		return Actions.redirect(ExampleRouteId.GET_LIST_USERS);
	}
	
	public Action listUsers() {
		service.fetchRemoteValue();
		return Actions.renderThis();
	}
}

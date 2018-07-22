package org.webpieces.plugins.backend.login;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.plugins.backend.menu.MenuCreator;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.routing.RouteId;
import org.webpieces.webserver.api.login.AbstractLoginController;

@Singleton
public class BackendLoginController extends AbstractLoginController {

	public static final String TOKEN = "backendUser";
	
	private BackendLogin login;
	private MenuCreator menuCreator;

	@Inject
	public BackendLoginController(BackendLogin login, MenuCreator menuCreator) {
		this.login = login;
		this.menuCreator = menuCreator;
	}
	
	@Override
	protected RouteId getRenderLoginRoute() {
		return BackendLoginRouteId.BACKEND_LOGIN;
	}

	@Override
	protected RouteId getRenderAfterLoginHome() {
		return BackendLoginRouteId.BACKEND_LOGGED_IN_HOME;
	}
	
	@Override
	protected boolean isValidLogin(String username, String password) {
		if(login.isLoginValid(username, password))
			return true;
		
		Current.flash().setError("Invalid username/password combination");
		return false;
	}
	
	@Override
	protected Action fetchGetLoginPageAction() {
		return Actions.renderView("/org/webpieces/plugins/backend/login/login.html", "menu", menuCreator.getMenu());
	}

	@Override
	protected String getLoginSessionKey() {
		return TOKEN;
	}
	
}

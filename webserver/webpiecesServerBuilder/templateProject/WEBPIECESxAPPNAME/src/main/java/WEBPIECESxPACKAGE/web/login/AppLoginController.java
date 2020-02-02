package WEBPIECESxPACKAGE.web.login;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.router.api.routes.RouteId;
import org.webpieces.webserver.api.login.AbstractLoginController;

@Singleton
public class AppLoginController extends AbstractLoginController {

	public static final String TOKEN = "userId";

	@Inject
	public AppLoginController() {
		super("password");
	}
	
	@Override
	protected boolean isValidLogin(String username, String password) {
		if(!"dean".equals(username)) {
			Current.flash().setError("No Soup for you!");
			Current.validation().addError("username", "I lied, Username must be 'dean'");
			return false;
		}
		
		return true;
	}
	
	@Override
	protected Action fetchGetLoginPageAction() {
		return Actions.renderView("/WEBPIECESxPACKAGE/web/login/login.html",
				"username", null,
				"password", null);
	}

	public Render loginHome() {
		return Actions.renderThis();
	}

	@Override
	protected String getLoginSessionKey() {
		return TOKEN;
	}

	@Override
	protected RouteId getRenderLoginRoute() {
		return LoginRouteId.LOGIN;
	}

	@Override
	protected RouteId getRenderAfterLoginHome() {
		return LoginRouteId.LOGGED_IN_HOME;
	}
}

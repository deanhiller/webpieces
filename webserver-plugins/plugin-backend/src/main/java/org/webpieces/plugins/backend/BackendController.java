package org.webpieces.plugins.backend;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugins.backend.login.BackendLoginRouteId;
import org.webpieces.plugins.backend.menu.MenuCreator;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class BackendController {

	private MenuCreator menuCreator;


	@Inject
	public BackendController(MenuCreator menuCreator) {
		this.menuCreator = menuCreator;
	}

	public Redirect redirectToLogin() {
		return Actions.redirect(BackendLoginRouteId.BACKEND_LOGIN);
	}

	public Redirect redirectHome() {
		return Actions.redirect(BackendLoginRouteId.BACKEND_LOGGED_IN_HOME);
	}
	
	public Render home() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}

	public Render internalError() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render notFound() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
}

package org.webpieces.plugins.backend;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugins.backend.login.BackendLoginRouteId;
import org.webpieces.plugins.backend.menu.MenuCreator;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Redirect;
import org.webpieces.router.api.actions.Render;

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

	public Render home() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}

}

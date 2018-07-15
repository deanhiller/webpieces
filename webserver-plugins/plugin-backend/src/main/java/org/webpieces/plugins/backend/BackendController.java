package org.webpieces.plugins.backend;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Render;

@Singleton
public class BackendController {

	private MenuCreator menuCreator;


	@Inject
	public BackendController(MenuCreator menuCreator) {
		this.menuCreator = menuCreator;
	}


	public Render home() {
		return Actions.renderThis("menus", menuCreator.getMenu());
	}

}

package org.webpieces.plugins.documentation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugins.backend.MenuCreator;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.actions.Render;

@Singleton
public class DocumentationController {

	private MenuCreator menuCreator;


	@Inject
	public DocumentationController(MenuCreator menuCreator) {
		this.menuCreator = menuCreator;
	}

	public Render mainDocs() {
		return Actions.renderThis("menus", menuCreator.getMenu());
	}

	public Render templates() {
		return Actions.renderThis("menus", menuCreator.getMenu());
	}
	
	public Render routes() {
		return Actions.renderThis("menus", menuCreator.getMenu());
	}
}

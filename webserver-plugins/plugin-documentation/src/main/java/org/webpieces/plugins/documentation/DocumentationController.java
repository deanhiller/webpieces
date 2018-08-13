package org.webpieces.plugins.documentation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugins.backend.menu.MenuCreator;
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
		return Actions.renderThis("menu", menuCreator.getMenu());
	}

	public Render templates() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render routes() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render controllers() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render design() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render quickStart() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render randomFeatures() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
}

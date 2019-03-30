package org.webpieces.plugins.documentation.plugins;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugins.backend.menu.MenuCreator;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class PluginController {

	private MenuCreator menuCreator;

	@Inject
	public PluginController(MenuCreator menuCreator) {
		this.menuCreator = menuCreator;
	}

	public Render plugins() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render jacksonPlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}

	public Render hibernatePlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render backendPlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render h2Plugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render installSslPlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render documentationPlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render propertiesPlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
	
	public Render codeGenPlugin() {
		return Actions.renderThis("menu", menuCreator.getMenu());
	}
}

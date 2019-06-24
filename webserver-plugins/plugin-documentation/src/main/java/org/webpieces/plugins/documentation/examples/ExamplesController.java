package org.webpieces.plugins.documentation.examples;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.plugins.backend.menu.MenuCreator;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class ExamplesController {

	private MenuCreator menuCreator;

	@Inject
	public ExamplesController(MenuCreator menuCreator) {
		this.menuCreator = menuCreator;
	}

	public Render inputText() {
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"firstName", null);
	}
	
	public Redirect postInputText(String firstName) {
		//We could put the firstName in the url such as /examples/inputResult/{firstName} 
		//or we could save to database
		//or we can put it in flash and for this example, we put it in flash
		Current.flash().put("firstName", firstName);
		Current.flash().keep();
		return Actions.redirect(ExampleRouteId.INPUT_TEXT_RESULT);
	}
	
	public Render inputTextResult() {
		String firstName = Current.flash().get("firstName");
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"firstName", firstName
				);
	}
	
}

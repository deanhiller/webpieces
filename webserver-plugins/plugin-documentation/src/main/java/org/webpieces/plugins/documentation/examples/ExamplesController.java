package org.webpieces.plugins.documentation.examples;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.Current;
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
	
	public Render checkbox() {
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"fun", true,
				"another", "checked");
	}
	
	public Redirect postCheckbox(String fun) {
		//We could put the firstName in the url such as /examples/inputResult/{firstName} 
		//or we could save to database
		//or we can put it in flash and for this example, we put it in flash
		Current.flash().put("isFun", fun);
		Current.flash().keep();
		return Actions.redirect(ExampleRouteId.CHECKBOX_RESULT);
	}
	
	public Render checkboxResult() {
		String message = "Other webservers are just not as cool";
		String isCool = Current.flash().get("isFun");
		if("true".equals(isCool))
			message =  "Webpieces is so cool";
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"message", message
				);
	}
	
	public Render enumList() {
		List<ColorEnum> colorList = Arrays.asList(ColorEnum.values());
		
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"colors", colorList,
				"selectedColor", ColorEnum.GREEN
				);
	}
	
	public Redirect postEnumList(String selectedColor) {
		//We could put the firstName in the url such as /examples/inputResult/{firstName} 
		//or we could save to database
		//or we can put it in flash and for this example, we put it in flash
		Current.flash().put("selectedColor", selectedColor);
		Current.flash().keep();
		return Actions.redirect(ExampleRouteId.ENUM_LIST_SINGLE_SELECT_RESULT);
	}
	
	public Render enumListResult() {
		String selectedColor = Current.flash().get("selectedColor");
		return Actions.renderThis(
				"menu", menuCreator.getMenu(),
				"selectedColor", selectedColor
				);
	}
}

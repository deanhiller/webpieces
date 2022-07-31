package webpiecesxxxxxpackage.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Render;

@Singleton
public class MainController {

	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	@Inject
	public MainController(){
	}
	public Render notFound() {
		return Actions.renderThis();
	}
	
	public Render internalError() {		
		Current.flash().clear();
		Current.validation().clear();
		return Actions.renderThis();
	}

}

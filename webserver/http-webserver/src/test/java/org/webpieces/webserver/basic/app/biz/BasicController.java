package org.webpieces.webserver.basic.app.biz;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.Actions;
import org.webpieces.router.api.controller.actions.Redirect;
import org.webpieces.router.api.controller.actions.Render;
import org.webpieces.http.exception.NotFoundException;
import org.webpieces.webserver.basic.app.BasicRouteId;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Singleton
public class BasicController {

	private final SomeOtherLib notFoundLib;
	private final SomeLib errorLib;
	private Counter counter;
	
	@Inject
	public BasicController(MeterRegistry metrics, SomeOtherLib notFoundLib, SomeLib errorLib) {
		super();
		this.notFoundLib = notFoundLib;
		this.errorLib = errorLib;
		
		counter = metrics.counter("basicCounter");
	}

	public Render reverseRoute() {
		int id = 5;
		String url = "";
		return Actions.renderThis("url", url);
	}

	public Action someMethod() {
		notFoundLib.someBusinessLogic();
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}
	
	public Action redirect(String id) {
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}
	
	public Action redirectWithInt(int id) {
		return Actions.redirect(BasicRouteId.RENDER_PAGE);
	}

	public Redirect redirectRawUrl() {
		return Actions.redirectToUrl("/myroute");
	}
	public Redirect redirectRawAbsoluteUrl() {
		return Actions.redirectToUrl("https://something.com/hi");
	}
	
	public Action throwNotFound() {
		throw new NotFoundException("not found");
	}
	
	public Action myMethod() {
		counter.increment();
		//renderThis assumes the view is the <methodName>.html file so in this case
		//myMethod.html which must be in the same directory as the Controller
		return Actions.renderThis("hhhh", 86);
	}
	
	public Render myMethodFullPath() {
		return Actions.renderView("/org/webpieces/webserver/basic/app/biz/myMethod.html");
	}
	
	public Action badTemplate() {
		return Actions.renderThis();
	}
	
	public Render notFound() {
		//we use this to mock and throw NotFoundException or some RuntimeException for testing notFound path failures
		notFoundLib.someBusinessLogic();
		return Actions.renderThis();
	}
	
	public Render internalError() {
		//we use this to mock and throw exceptions when needed for testing
		errorLib.someBusinessLogic();
		return Actions.renderThis();
	}
	
	public Action jsonFile() {
		return Actions.renderView("basic.json");
	}
	
	public Action returnNull() {
		return null;
	}
}

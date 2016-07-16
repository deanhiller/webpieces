package org.webpieces.webserver.sync.error;

import java.util.List;

import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.Actions;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.Router;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.webserver.sync.BasicAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class NotFoundMeta implements WebAppMeta {
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new BasicAppMeta.BasicModule());
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new NotFoundRouteModule());
	}
	
	private static class NotFoundRouteModule implements RouteModule {
		@Override
		public void configure(Router router, String currentPackage) {
			router.setPageNotFoundRoute("NotFoundMeta$NotFoundThrowsController.notFound");
			router.setInternalErrorRoute("NotFoundMeta$NotFoundThrowsController.internalError2");
		}
	}
	
	public static class NotFoundThrowsController {
		
		public Action notFound() {
			throw new NotFoundException("apps should not do this, but if they do, let's make sue we handle it propertly");
		}

		public Action internalError2() {
			return Actions.renderThis();
		}
	}
	
	
}

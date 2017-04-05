package org.webpieces.templatingdev.api;

import java.util.Map;

import org.webpieces.templating.api.RouterLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

public class RouterLookupModule implements Module {
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RouterLookup.class).to(NullRouterLookup.class).asEagerSingleton();
	}
	
	private static class NullRouterLookup implements RouterLookup {
		@Override
		public String fetchUrl(String routeId, Map<String, String> args) {
			return null;
		}

		@Override
		public String pathToUrlEncodedHash(String relativeUrlPath) {
			return null;
		}
	}
}

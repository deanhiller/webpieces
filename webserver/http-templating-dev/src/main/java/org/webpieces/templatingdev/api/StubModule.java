package org.webpieces.templatingdev.api;

import java.util.Map;

import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.api.RouterLookup;

import com.google.inject.Binder;
import com.google.inject.Module;

public class StubModule implements Module {
	
	@Override
	public void configure(Binder binder) {
		binder.bind(RouterLookup.class).to(NullRouterLookup.class).asEagerSingleton();
		binder.bind(ConverterLookup.class).to(NullConverterLookup.class).asEagerSingleton();
	}

	private static class NullConverterLookup implements ConverterLookup {
		@Override
		public String convert(Object value) {
			return null;
		}
	}
	
	private static class NullRouterLookup implements RouterLookup {
		@Override
		public String fetchUrl(String routeId, Map<String, Object> args) {
			return null;
		}

		@Override
		public String pathToUrlEncodedHash(String relativeUrlPath) {
			return null;
		}
	}
}

package org.webpieces.plugins.backend;

import org.webpieces.router.api.SimpleStorage;

import com.google.inject.Binder;
import com.google.inject.Module;

public class EmptyModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
	}

}

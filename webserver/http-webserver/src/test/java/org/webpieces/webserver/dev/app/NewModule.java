package org.webpieces.webserver.dev.app;

import com.google.inject.Binder;
import com.google.inject.Module;

public class NewModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(NewInterface.class).to(NewLibrary.class);
	}

}

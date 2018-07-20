package org.webpieces.plugins.backend;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class BackendModule implements Module {

	@Override
	public void configure(Binder binder) {
		//create empty set in case no other plugins are installed so we work with 0 plugins though that is quite boring
		Multibinder.newSetBinder(binder, BackendGuiDescriptor.class);

	}

}

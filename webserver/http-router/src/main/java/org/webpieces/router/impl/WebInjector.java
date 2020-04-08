package org.webpieces.router.impl;

import javax.inject.Singleton;

import org.webpieces.ctx.api.ApplicationContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

@Singleton
public class WebInjector {

	private Injector injector;

	public Injector createInjector(Module module) {
		injector = Guice.createInjector(module);
		
		//test For AppContext, if not exist, this throws exception
		getAppContext();
		
		return injector;
	}
	
	public ApplicationContext getAppContext() {
		//MUST re-fetch each time in Development server!!...
		return injector.getInstance(ApplicationContext.class);
	}

}

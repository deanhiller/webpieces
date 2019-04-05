package org.webpieces.router.impl.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.router.impl.services.ServiceInvoker;

@Singleton
public class SvcProxyLogic {

	private final RouterConfig config;
	private final ServiceInvoker serviceInvoker;
	private final ParamToObjectTranslatorImpl translator;

	@Inject
	public SvcProxyLogic( 
		RouterConfig config, 
		ServiceInvoker serviceInvoker, 
		ParamToObjectTranslatorImpl translator
	) {
		this.config = config;
		this.serviceInvoker = serviceInvoker;
		this.translator = translator;
	}

	public RouterConfig getConfig() {
		return config;
	}

	public ServiceInvoker getServiceInvoker() {
		return serviceInvoker;
	}

	public ParamToObjectTranslatorImpl getTranslator() {
		return translator;
	}
	
}

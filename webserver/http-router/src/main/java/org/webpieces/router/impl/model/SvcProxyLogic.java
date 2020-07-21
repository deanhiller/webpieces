package org.webpieces.router.impl.model;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.impl.params.BeanValidator;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.router.impl.services.ControllerInvoker;

@Singleton
public class SvcProxyLogic {

	private final RouterConfig config;
	private final ControllerInvoker serviceInvoker;
	private final ParamToObjectTranslatorImpl translator;
	private BeanValidator validator;

	@Inject
	public SvcProxyLogic( 
		RouterConfig config, 
		ControllerInvoker serviceInvoker, 
		ParamToObjectTranslatorImpl translator,
		BeanValidator validator
	) {
		this.config = config;
		this.serviceInvoker = serviceInvoker;
		this.translator = translator;
		this.validator = validator;
	}

	public RouterConfig getConfig() {
		return config;
	}

	public ControllerInvoker getServiceInvoker() {
		return serviceInvoker;
	}

	public ParamToObjectTranslatorImpl getTranslator() {
		return translator;
	}

	public BeanValidator getValidator() {
		return validator;
	}
	
}

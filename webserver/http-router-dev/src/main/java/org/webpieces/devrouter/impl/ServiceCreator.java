package org.webpieces.devrouter.impl;

import javax.inject.Inject;

import org.webpieces.router.api.RouterConfig;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.impl.body.BodyParsers;
import org.webpieces.router.impl.loader.ServiceProxy;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;
import org.webpieces.util.filters.Service;

public class ServiceCreator {

	private RouterConfig config;
	private ParamToObjectTranslatorImpl translator;
	private BodyParsers requestBodyParsers;

	@Inject
	public ServiceCreator(
			RouterConfig config,
			ParamToObjectTranslatorImpl translator,
			BodyParsers requestBodyParsers
	) {
		this.config = config;
		this.translator = translator;
		this.requestBodyParsers = requestBodyParsers;
	}

	public Service<MethodMeta, Action> create() {
		return new ServiceProxy(translator, requestBodyParsers, config);
	}
}

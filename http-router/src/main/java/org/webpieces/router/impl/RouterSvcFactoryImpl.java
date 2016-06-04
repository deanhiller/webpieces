package org.webpieces.router.impl;

import org.webpieces.router.api.RouterSvcFactory;
import org.webpieces.router.api.RoutingService;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Module;

public class RouterSvcFactoryImpl extends RouterSvcFactory {

	@Override
	protected RoutingService createImpl(VirtualFile modules, Module overrideModule) {
		return new RouterSvcImpl(modules, overrideModule);
	}

}

package org.webpieces.router.api;

import org.webpieces.router.api.file.VirtualFile;
import org.webpieces.router.impl.RouterSvcFactoryImpl;

import com.google.inject.Module;

public abstract class RouterSvcFactory {
	
    protected RouterSvcFactory() {}

    public static RoutingService create(VirtualFile modules) {
    	return create(modules, null);
    }
    
	public static RoutingService create(VirtualFile modules, Module overrideModule) {
		RouterSvcFactory factory = new RouterSvcFactoryImpl();
		RoutingService proxyImpl = factory.createImpl(modules, overrideModule);
		return proxyImpl;	
	}

	protected abstract RoutingService createImpl(VirtualFile modules, Module overrideModule);
	
}

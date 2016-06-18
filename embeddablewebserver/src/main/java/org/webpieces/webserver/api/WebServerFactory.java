package org.webpieces.webserver.api;

import org.webpieces.webserver.impl.WebServerFactoryImpl;

import com.google.inject.Module;

public abstract class WebServerFactory {
	
	public static final String IMPL_CLASS_KEY = "org.webpieces.HttpProxyFactory.impl.string";
	public static final String OVERRIDE_MODULE = "org.webpices.HttpProxy.modules.Module";
	
    protected WebServerFactory() {}

    public static WebServer create(String id, WebServerConfig config) {
    	return create(id, null, config);
    }
    
	public static WebServer create(String id, Module overrideModule, WebServerConfig config) {
		WebServerFactory factory = new WebServerFactoryImpl();
		WebServer proxyImpl = factory.createImpl(id, overrideModule, config);
		return proxyImpl;	
	}

	protected abstract WebServer createImpl(String id, Module overrideModule, WebServerConfig config);
	
}

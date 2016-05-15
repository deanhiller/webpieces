package org.webpieces.httpproxy.api;

import org.webpieces.httpproxy.impl.HttpProxyFactoryImpl;

import com.google.inject.Module;

public abstract class HttpProxyFactory {
	
	public static final String IMPL_CLASS_KEY = "org.webpieces.HttpProxyFactory.impl.string";
	public static final String OVERRIDE_MODULE = "org.webpices.HttpProxy.modules.Module";
	
    protected HttpProxyFactory() {}

    public static HttpProxy createHttpProxy(String id, ProxyConfig config) {
    	return createHttpProxy(id, null, config);
    }
    
	public static HttpProxy createHttpProxy(String id, Module overrideModule, ProxyConfig config) {
		HttpProxyFactory factory = new HttpProxyFactoryImpl();
		HttpProxy proxyImpl = factory.createHttpProxyImpl(id, overrideModule, config);
		return proxyImpl;	
	}

	protected abstract HttpProxy createHttpProxyImpl(String id, Module overrideModule, ProxyConfig config);
	
}

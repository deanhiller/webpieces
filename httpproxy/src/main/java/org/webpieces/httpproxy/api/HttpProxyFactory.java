package org.webpieces.httpproxy.api;

import java.util.Map;

public abstract class HttpProxyFactory {
	
	public static final String IMPL_CLASS_KEY = "org.webpieces.HttpProxyFactory.impl.string";
	public static final String OVERRIDE_MODULE = "org.webpices.HttpProxy.modules.Module";
	
    private static final String DEFAULT_IMPL = "org.webpieces.httpproxy.impl.HttpProxyFactoryImpl";

    protected HttpProxyFactory() {}
	
	public static HttpProxy createHttpProxy(String id, Map<String, Object> props) {
		String implClazz = (String) props.get(IMPL_CLASS_KEY);
		if(implClazz == null) {
			implClazz = DEFAULT_IMPL;
		}
		
		HttpProxyFactory factory;
		try {
			Class<?> classInst = Class.forName(implClazz);
			factory = (HttpProxyFactory) classInst.newInstance();
		} catch (Throwable  e) {
			throw new RuntimeException(e);
		}
		return factory.createHttpProxyImpl(id, props);		
	}

	protected abstract HttpProxy createHttpProxyImpl(String id, Map<String, Object> props);
	
	protected abstract HttpProxyService createHttpProxyService();
}

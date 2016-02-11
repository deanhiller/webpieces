package org.playorm.util.api.serviceproxy;

import java.lang.reflect.Proxy;

public final class ServiceProxyFactory {

	private ServiceProxyFactory() {
		// TODO Auto-generated constructor stub
	}
	
	//realService should either be an instance of StartableService or a 
	//class with a start and stop method!!!
	//serviceIntclass should be the interface with Start/stop method OR
	//StartableService.class if you are using a customer proxy
	public static ServiceProxy createServiceProxy(Class serviceIntfClass, Object realService) {
		if(CustomServiceProxy.class.isAssignableFrom(realService.getClass()))
			throw new IllegalArgumentException("You should use createCustomServiceProxy instead");
		//TODO: we should check if interface has start/stop methods here
		//first and tell them to use createCustom if they don't!!!
		return (ServiceProxy) (Proxy.newProxyInstance(ServiceProxy.class
				.getClassLoader(), new Class[] { serviceIntfClass, ServiceProxy.class },
				new CustomServiceProxy(realService)));
	}

	public static ServiceProxy createCustomServiceProxy(Class serviceIntfClass, CustomServiceProxy handler) {
		return (ServiceProxy) Proxy.newProxyInstance(ServiceProxy.class
				.getClassLoader(), new Class[] { ServiceProxy.class, serviceIntfClass },
				handler);
	}
}

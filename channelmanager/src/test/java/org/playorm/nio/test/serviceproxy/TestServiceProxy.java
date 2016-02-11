package org.playorm.nio.test.serviceproxy;


import junit.framework.TestCase;

import org.playorm.util.api.serviceproxy.CustomServiceProxy;
import org.playorm.util.api.serviceproxy.ServiceProxy;
import org.playorm.util.api.serviceproxy.ServiceProxyFactory;

import biz.xsoftware.mock.MockObject;
import biz.xsoftware.mock.MockObjectFactory;

public class TestServiceProxy extends TestCase {

	private MockObject mockService;
	
	private MockObject mockService2;
	
	private ServiceProxy proxy;

	@Override
	protected void setUp() throws Exception {
		mockService = MockObjectFactory.createMock(ServiceIntf.class);
		mockService2 = MockObjectFactory
		.createMock(ServiceIntf.class);
		ServiceIntf service = (ServiceIntf) ServiceProxyFactory
				.createServiceProxy(ServiceIntf.class, mockService);
	 	proxy = (ServiceProxy)service;
	}
	
	@Override
	protected void tearDown() throws Exception {
		mockService.expect(MockObject.NONE);
		mockService2.expect(MockObject.NONE);
	}
	public void testCreateByContructorAndServiceCanStart(){
		((ServiceIntf)proxy).start();
		mockService.expect("start");
		((ServiceIntf)proxy).stop();
		mockService.expect("stop");
	}

	public void testCreateBySet() {
		proxy.setService(mockService2);
		((ServiceIntf)proxy).start();
		((ServiceIntf)proxy).stop();
	}
	
	public void testUnset(){
		proxy.setService(mockService2);
		proxy.unsetService(mockService);
		((ServiceIntf)proxy).start();
		mockService.expect("start");
	}
	
	public void testDefaultUserService(){
		((ServiceIntf)proxy).serviceMethod();
		mockService.expect("serviceMethod");
	}
	
	public void testUserService(){
		proxy.setService(mockService2);
		((ServiceIntf)proxy).serviceMethod();
		mockService2.expect("serviceMethod");
	}
	
	public void testCustomServiceProxy() {
		ServiceProxy customProxy = (ServiceProxy) ServiceProxyFactory
				.createCustomServiceProxy(ServiceIntf.class, 
						new FakeCustromProxy((ServiceIntf)mockService));
		customProxy.start();
		mockService.expect("start");
	}
	
	private class FakeCustromProxy extends CustomServiceProxy {

		public FakeCustromProxy(ServiceIntf service) {
			super(service);
		}

		@Override
		public void start() {
			((ServiceIntf)service).start();
			
		}

		@Override
		public void stop() {
			((ServiceIntf)service).stop();			
		}
	}
}

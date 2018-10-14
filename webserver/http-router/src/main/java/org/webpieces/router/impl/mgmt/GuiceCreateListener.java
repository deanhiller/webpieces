package org.webpieces.router.impl.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.spi.InjectionListener;

@SuppressWarnings("rawtypes")
public class GuiceCreateListener implements InjectionListener {

	private Logger log = LoggerFactory.getLogger(GuiceCreateListener.class);
	
	private ManagedBeanMeta proxy;
	private Class<?> interfaze;
	

	public GuiceCreateListener(ManagedBeanMeta proxy, Class<?> interfaze) {
		this.proxy = proxy;
		this.interfaze = interfaze;
	}


	@Override
	public void afterInjection(Object injectee) {
		log.info("hearing object all setup="+injectee.getClass().getName()+" and interface="+interfaze);
		proxy.afterInjection(injectee, interfaze);
	}

}

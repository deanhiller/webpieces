package org.webpieces.plugins.properties.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.spi.InjectionListener;

@SuppressWarnings("rawtypes")
public class GuiceListener implements InjectionListener {

	private Logger log = LoggerFactory.getLogger(GuiceListener.class);
	private BeanMetaData proxy;
	private Class<?> interfaze;
	
	public GuiceListener(BeanMetaData proxy, Class<?> interfaze) {
		this.proxy = proxy;
		this.interfaze = interfaze;
	}

	@Override
	public void afterInjection(Object injectee) {
		log.info("hearing object all setup="+injectee.getClass().getName()+" and interface="+interfaze);
		proxy.afterInjection(injectee, interfaze);
	}
}

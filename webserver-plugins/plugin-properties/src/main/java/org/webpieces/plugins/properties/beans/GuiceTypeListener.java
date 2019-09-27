package org.webpieces.plugins.properties.beans;

import java.util.Arrays;
import java.util.List;

import org.webpieces.plugins.properties.PropertiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceTypeListener implements TypeListener {
	private Logger log = LoggerFactory.getLogger(GuiceTypeListener.class);
	
	private BeanMetaData proxy;
	private PropertiesConfig config;

	public GuiceTypeListener(BeanMetaData proxy, PropertiesConfig config) {
		this.proxy = proxy;
		this.config = config;
	}

	@SuppressWarnings("unchecked")
	public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
		Class<?> clazz = typeLiteral.getRawType();
		Class<?>[] interfaces = clazz.getInterfaces();
		List<Class<?>> interfazes = Arrays.asList(interfaces);
		log.info("class="+clazz+" interfaces="+interfazes);
		for(Class<?> interfaze : interfaces) {
			if(interfaze.getSimpleName().endsWith(config.getInterfaceSuffixMatch())) {
				log.info("FOUND a managed bean="+clazz);
				typeEncounter.register(new GuiceListener(proxy, interfaze));				
			}
		}
	}
}

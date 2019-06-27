package org.webpieces.plugins.properties.beans;

import org.webpieces.plugins.properties.PropertiesConfig;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

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
		log.info("class="+clazz+" interfaces="+interfaces);
		for(Class<?> interfaze : interfaces) {
			if(interfaze.getSimpleName().endsWith(config.getInterfaceSuffixMatch())) {
				log.info("FOUND a managed bean="+clazz);
				typeEncounter.register(new GuiceListener(proxy, interfaze));				
			}
		}
	}
}

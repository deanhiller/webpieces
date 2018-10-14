package org.webpieces.router.impl.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceWebpiecesListener implements TypeListener {
	private Logger log = LoggerFactory.getLogger(GuiceWebpiecesListener.class);
	
	private ManagedBeanMeta proxy;

	public GuiceWebpiecesListener(ManagedBeanMeta proxy) {
		this.proxy = proxy;
	}

	@SuppressWarnings("unchecked")
	public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
		Class<?> clazz = typeLiteral.getRawType();
		Class<?>[] interfaces = clazz.getInterfaces();
		log.info("webpieces platform class="+clazz+" interfaces="+interfaces);
		for(Class<?> interfaze : interfaces) {
			if(interfaze.getSimpleName().endsWith("WebManaged")) {
				log.info("FOUND a managed bean="+clazz);
				typeEncounter.register(new GuiceCreateListener(proxy, interfaze));
			}
		}
	}
}

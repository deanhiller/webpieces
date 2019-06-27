package org.webpieces.router.impl.mgmt;

import java.util.Arrays;
import java.util.List;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

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
		List<Class<?>> interfaceList = Arrays.asList(interfaces);
		log.trace(() -> "webpieces platform class="+clazz+" interfaces="+interfaceList);
		for(Class<?> interfaze : interfaces) {
			if(interfaze.getSimpleName().endsWith("WebManaged")) {
				log.info("FOUND a managed bean="+clazz);
				typeEncounter.register(new GuiceCreateListener(proxy, interfaze));
			}
		}
	}
}

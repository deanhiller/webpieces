package org.webpieces.router.impl.mgmt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class ManagedBeanMeta {

	private List<CachedBean> cachedWebpiecesBeans = new ArrayList<>();
			
	public void afterInjection(Object injectee, Class<?> interfaze) {
		cachedWebpiecesBeans.add(new CachedBean(injectee, interfaze));
	}

	//For plugins or anyone to fetch the platform beans to wire into managing them
	public List<CachedBean> getBeans() {
		return cachedWebpiecesBeans;
	}

}

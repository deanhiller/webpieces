package org.webpieces.router.impl.mgmt;

public class CachedBean {

	private Object injectee;
	private Class<?> interfaze;

	public CachedBean(Object injectee, Class<?> interfaze) {
		this.injectee = injectee;
		this.interfaze = interfaze;
	}

	public Object getInjectee() {
		return injectee;
	}

	public Class<?> getInterfaze() {
		return interfaze;
	}
}

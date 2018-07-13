package org.webpieces.plugins.hibernate;

import javax.persistence.EntityManager;

public class Em {

	private static ThreadLocal<EntityManager> emThreadLocal = new ThreadLocal<>();
	
	public static void set(EntityManager em) {
		emThreadLocal.set(em);
	}

	public static EntityManager get() {
		return emThreadLocal.get();
	}
}

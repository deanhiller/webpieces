package org.webpieces.router.impl.loader;

import javax.inject.Singleton;

@Singleton
public class ProdClassForName implements ClassForName {

	@Override
	public Class<?> clazzForName(String clazzName) {
		try {
			return Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Your clazz="+clazzName+" was not found on the classpath", e);
		}
	}

}

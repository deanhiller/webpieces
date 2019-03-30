package org.webpieces.router.impl.model;

import org.webpieces.router.api.routing.BasicRoutes;
import org.webpieces.router.api.routing.Routes;

public class RouteModuleInfo {

	public String packageName;
	public String i18nBundleName;

	public RouteModuleInfo(Class<?> moduleClazz) {
		if(!Routes.class.isAssignableFrom(moduleClazz) &&
				!BasicRoutes.class.isAssignableFrom(moduleClazz))
				throw new IllegalArgumentException("Must be of type Routes.class or BasicRoutes.class");
		this.packageName = getPackage(moduleClazz);
		this.i18nBundleName = getI18nBundleName(moduleClazz);
	}
	
	public RouteModuleInfo(String packageName, String i18n) {
		this.packageName = packageName;
		this.i18nBundleName = i18n;		
	}

	/**
	 * This is the bundle name as in something like org.webpieces.messages where
	 * that will use org/webpieces/messages.properties on the classpath for the default
	 * locale or another messages file for another language
	 * @param module 
	 */
	public String getI18nBundleName(Class<?> clazz) {
		String name = clazz.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if(lastIndexOf < 0)
			return "messages";
		
		String packageName = name.substring(0, lastIndexOf);
		return packageName+".messages";
	}
	
	private String getPackage(Class<?> clazz) {
		int lastIndexOf = clazz.getName().lastIndexOf(".");
		String pkgName = clazz.getName().substring(0, lastIndexOf);
		return pkgName;
	}
}

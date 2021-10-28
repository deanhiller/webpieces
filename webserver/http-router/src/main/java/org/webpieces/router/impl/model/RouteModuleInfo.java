package org.webpieces.router.impl.model;

import org.webpieces.router.api.routes.BasicRoutes;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.impl.routebldr.ProcessCors;

import java.util.ArrayList;
import java.util.List;

public class RouteModuleInfo {

	private String packageName;
	private String i18nBundleName;
	private ProcessCors corsProcessor;

	public RouteModuleInfo(Class<?> moduleClazz) {
		if(!Routes.class.isAssignableFrom(moduleClazz) &&
				!BasicRoutes.class.isAssignableFrom(moduleClazz))
				throw new IllegalArgumentException("Must be of type Routes.class or BasicRoutes.class");
		
		int lastIndexOf1 = moduleClazz.getName().lastIndexOf(".");
		this.packageName = moduleClazz.getName().substring(0, lastIndexOf1);
		
		String name = moduleClazz.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if(lastIndexOf < 0) {
			packageName = "messages";
			return;
		}
		
		String packageName = name.substring(0, lastIndexOf);
		this.i18nBundleName = packageName+".messages";
	}
	
	public RouteModuleInfo(String packageName, String i18n) {
		this.packageName = packageName;
		this.i18nBundleName = i18n;		
	}

	public String getPackageName() {
		return packageName;
	}

	public String getI18nBundleName() {
		return i18nBundleName;
	}

	public void setCorProcessor(ProcessCors corsProcessor) {
		this.corsProcessor = corsProcessor;
	}

	public ProcessCors getCorsProcessor() {
		return corsProcessor;
	}
}

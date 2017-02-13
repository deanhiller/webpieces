package org.webpieces.router.impl.model;

public class RouteModuleInfo {

	public String packageName;
	public String i18nBundleName;

	public RouteModuleInfo(String packageName, String i18nBundleName) {
		if(packageName == null)
			throw new IllegalArgumentException("packageName must be non-null");
		
		this.packageName = packageName;
		this.i18nBundleName = i18nBundleName;
	}
}

package org.webpieces.plugins.properties;

public class PropertiesConfig {

	private String interfaceSuffixMatch = "WebpiecesManaged";
	private String categoryMethod = "getCategory";
	
	public PropertiesConfig() {
	}

	public PropertiesConfig(String interfaceSuffixMatch) {
		this.setInterfaceSuffixMatch(interfaceSuffixMatch);
	}

	public String getInterfaceSuffixMatch() {
		return interfaceSuffixMatch;
	}

	public void setInterfaceSuffixMatch(String interfaceSuffixMatch) {
		this.interfaceSuffixMatch = interfaceSuffixMatch;
	}

	public String getCategoryMethod() {
		return categoryMethod;
	}

	public void setCategoryMethod(String categoryMethod) {
		this.categoryMethod = categoryMethod;
	}
	
}

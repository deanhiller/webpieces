package org.webpieces.plugin.secure.properties;

public class PropertiesConfig {

	private String interfaceSuffixMatch = "Managed";
	private String categoryMethod = "getCategory";
	private int pollIntervalSeconds = 60;
	
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

	public int getPollIntervalSeconds() {
		return pollIntervalSeconds;
	}

	public void setPollIntervalSeconds(int pollIntervalSeconds) {
		this.pollIntervalSeconds = pollIntervalSeconds;
	}
	
}

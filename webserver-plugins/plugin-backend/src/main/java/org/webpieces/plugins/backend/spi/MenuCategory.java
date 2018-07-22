package org.webpieces.plugins.backend.spi;

public enum MenuCategory {

	MANAGEMENT("Management"), DOCUMENTATION("Documentation");
	
	private String title;

	MenuCategory(String title) {
		this.title = title;
	}
	
	public String getMenuTitle() {
		return title;
	}
	
	public String getLowerCaseMenuTitle() {
		return title.toLowerCase();
	}
}

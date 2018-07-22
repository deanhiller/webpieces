package org.webpieces.plugins.backend.menu;

public class SingleMenuItem {

	private String menuTitle;
	private String url;

	public SingleMenuItem(String menuTitle, String url) {
		super();
		this.menuTitle = menuTitle;
		this.url = url;
	}

	public String getMenuTitle() {
		return menuTitle;
	}

	public String getUrl() {
		return url;
	}
	
}

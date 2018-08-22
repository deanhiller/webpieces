package org.webpieces.plugins.backend.menu;

public class SingleMenuItem {

	private String menuTitle;
	private String url;
	private boolean isSecure;

	public SingleMenuItem(String menuTitle, String url, boolean isSecure) {
		super();
		this.menuTitle = menuTitle;
		this.url = url;
		this.isSecure = isSecure;
	}

	public String getMenuTitle() {
		return menuTitle;
	}

	public String getUrl() {
		return url;
	}

	public boolean isSecure() {
		return isSecure;
	}

}

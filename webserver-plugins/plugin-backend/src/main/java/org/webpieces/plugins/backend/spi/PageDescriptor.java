package org.webpieces.plugins.backend.spi;

import org.webpieces.plugins.backend.BackendController;
import org.webpieces.plugins.backend.BackendRoutes;

public class PageDescriptor {

	private MenuCategory category;
	private String menuTitle;
	private String relativeUrl;
	
	public PageDescriptor(MenuCategory category, String menuTitle, String relativeUrl) {
		super();
		this.category = category;
		this.menuTitle = menuTitle;
		this.relativeUrl = relativeUrl;
	}
	
	public MenuCategory getMenuCategory() {
		return category;
	}
	
	public String getMenuTitle() {
		return menuTitle;
	}
	
	public String getLowerCaseMenuTitle() {
		return menuTitle.toLowerCase();
	}
	
	public String getRelativeUrl() {
		return BackendRoutes.BACKEND_ROUTE+relativeUrl;
	}
}

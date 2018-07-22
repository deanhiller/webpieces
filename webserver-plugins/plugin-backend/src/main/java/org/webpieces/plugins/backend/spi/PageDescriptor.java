package org.webpieces.plugins.backend.spi;

import org.webpieces.router.api.routing.RouteId;

public class PageDescriptor {

	private MenuCategory category;
	private String menuTitle;
	private RouteId routeId;
	private boolean isSecure;
	
	public PageDescriptor(MenuCategory category, String menuTitle, RouteId routeId) {
		this(category, menuTitle, routeId, true);
	}

	public PageDescriptor(MenuCategory category, String menuTitle, RouteId routeId, boolean isSecure) {
		super();
		this.category = category;
		this.menuTitle = menuTitle;
		this.routeId = routeId;
		this.isSecure = isSecure;
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

	public RouteId getRouteId() {
		return routeId;
	}

	public boolean isSecure() {
		return isSecure;
	}
	
}

package org.webpieces.plugins.backend.menu;

import java.util.List;

import org.webpieces.plugins.backend.spi.MenuCategory;

public class SingleMenu {

	private MenuCategory menuCategory;
	private List<SingleMenuItem> menuItems;
	private boolean allSecure;

	public SingleMenu(MenuCategory menuCategory, List<SingleMenuItem> menuItems, boolean allSecure) {
		this.menuCategory = menuCategory;
		this.menuItems = menuItems;
		this.allSecure = allSecure;
	}

	public MenuCategory getMenuCategory() {
		return menuCategory;
	}

	public void setMenuCategory(MenuCategory menuCategory) {
		this.menuCategory = menuCategory;
	}

	public List<SingleMenuItem> getMenuItems() {
		return menuItems;
	}

	public boolean isAllSecure() {
		return allSecure;
	}

}

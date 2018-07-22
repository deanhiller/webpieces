package org.webpieces.plugins.backend.menu;

import java.util.List;

import org.webpieces.plugins.backend.spi.MenuCategory;

public class SingleMenu {

	private MenuCategory menuCategory;
	private List<SingleMenuItem> menuItems;

	public SingleMenu(MenuCategory menuCategory, List<SingleMenuItem> menuItems) {
		this.menuCategory = menuCategory;
		this.menuItems = menuItems;
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

}

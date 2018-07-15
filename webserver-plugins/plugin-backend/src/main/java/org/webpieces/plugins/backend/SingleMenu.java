package org.webpieces.plugins.backend;

import java.util.List;

import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;

public class SingleMenu {

	private MenuCategory menuCategory;
	private List<PageDescriptor> menuItems;

	public SingleMenu(MenuCategory menuCategory, List<PageDescriptor> menuItems) {
		this.setMenuCategory(menuCategory);
		this.setMenuItems(menuItems);
	}

	public MenuCategory getMenuCategory() {
		return menuCategory;
	}

	public void setMenuCategory(MenuCategory menuCategory) {
		this.menuCategory = menuCategory;
	}

	public List<PageDescriptor> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<PageDescriptor> menuItems) {
		this.menuItems = menuItems;
	}

}

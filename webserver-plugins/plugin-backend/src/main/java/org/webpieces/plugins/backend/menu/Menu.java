package org.webpieces.plugins.backend.menu;

import java.util.List;

public class Menu {
	private List<SingleMenu> secureMenu;
	private List<SingleMenu> publicMenu;

	public Menu(List<SingleMenu> secureMenu, List<SingleMenu> publicMenu) {
		super();
		this.secureMenu = secureMenu;
		this.publicMenu = publicMenu;
	}
	public List<SingleMenu> getSecureMenu() {
		return secureMenu;
	}
	public List<SingleMenu> getPublicMenu() {
		return publicMenu;
	}
	
}

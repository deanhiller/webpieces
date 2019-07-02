package org.webpieces.plugins.backend.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;
import org.webpieces.router.api.plugins.ReverseRouteLookup;
import org.webpieces.router.impl.RoutingHolder;

@Singleton
public class MenuCreator {

	private List<SingleMenu> menus;
	private RoutingHolder routes;
	private Set<BackendGuiDescriptor> descriptors;

	@Inject
	public MenuCreator(RoutingHolder routes, Set<BackendGuiDescriptor> descriptors) {
		this.routes = routes;
		this.descriptors = descriptors;
	}
	
	private SingleMenu convert(Entry<MenuCategory, List<SingleMenuItem>> entry) {
		boolean allSecure = true;
		for(SingleMenuItem item : entry.getValue()) {
			if(!item.isSecure())
				allSecure = false;
		}
		
		return new SingleMenu(entry.getKey(), entry.getValue(), allSecure);
	}
	
	private void wireInPages(ReverseRouteLookup reverseRouteLookup, Map<MenuCategory, List<SingleMenuItem>> secureMenuMap, BackendGuiDescriptor desc) {
		for(PageDescriptor pageDesc : desc.getWireIntoGuiDescriptors()) {
			List<SingleMenuItem> descriptors = secureMenuMap.getOrDefault(pageDesc.getMenuCategory(), new ArrayList<>());

			if(!reverseRouteLookup.isGetRequest(pageDesc.getRouteId()))
				throw new RuntimeException("Plugin "+desc.getPluginName()+" supplied an illegal route id that is not a GET request="+pageDesc.getRouteId());
			
			String url = reverseRouteLookup.convertToUrl(pageDesc.getRouteId());
			
			descriptors.add(new SingleMenuItem(pageDesc.getMenuTitle(), url, pageDesc.isSecure()));
			secureMenuMap.putIfAbsent(pageDesc.getMenuCategory(), descriptors);
		}
	}
	
	public synchronized List<SingleMenu> getMenu() {
		if(menus == null)
			createMenuOnce();
		
		return menus;
	}

	private void createMenuOnce() {
		Map<MenuCategory, List<SingleMenuItem>> menuMap = new HashMap<>();

		for(BackendGuiDescriptor desc : descriptors) {
			wireInPages(routes.getReverseRouteLookup(), menuMap, desc);
		}
		
		menus = create(menuMap);
	}

	private List<SingleMenu> create(Map<MenuCategory, List<SingleMenuItem>> secureMenuMap) {
		List<SingleMenu> menu = new ArrayList<>();

		for(Map.Entry<MenuCategory, List<SingleMenuItem>> entry : secureMenuMap.entrySet()) {
			menu.add(convert(entry));
		}
		
		//since it was converted to Map, sort to put alphabetically.  The items in a menu sort order is
		//the way the plugins are ordered and the way descriptors are added
		menu.sort(new HeadingCompare());
		return menu;
	}
	
}

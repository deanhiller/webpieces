package org.webpieces.plugins.backend.menu;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;
import org.webpieces.router.api.routing.ReverseRouteLookup;
import org.webpieces.router.impl.RouteMeta;
import org.webpieces.router.impl.RoutingHolder;

@Singleton
public class MenuCreator {

	private Menu menu;
	private RoutingHolder routes;
	private Set<BackendGuiDescriptor> descriptors;

	@Inject
	public MenuCreator(RoutingHolder routes, Set<BackendGuiDescriptor> descriptors) {
		this.routes = routes;
		this.descriptors = descriptors;
	}
	
	private SingleMenu convert(Entry<MenuCategory, List<SingleMenuItem>> entry) {
		return new SingleMenu(entry.getKey(), entry.getValue());
	}
	
	private void wireInSecurePages(ReverseRouteLookup reverseRouteLookup, Map<MenuCategory, List<SingleMenuItem>> secureMenuMap, BackendGuiDescriptor desc) {
		for(PageDescriptor pageDesc : desc.getWireIntoGuiDescriptors()) {
			if(!pageDesc.isSecure())
				continue;
			
			List<SingleMenuItem> descriptors = secureMenuMap.getOrDefault(pageDesc.getMenuCategory(), new ArrayList<>());
			RouteMeta meta = reverseRouteLookup.get(pageDesc.getRouteId());
			if(meta.getRoute().getHttpMethod() != HttpMethod.GET)
				throw new RuntimeException("Plugin "+desc.getPluginName()+" supplied an illegal route id that is not a GET request="+pageDesc.getRouteId());
			
			String url = meta.getRoute().getFullPath();
			
			descriptors.add(new SingleMenuItem(pageDesc.getMenuTitle(), url));
			secureMenuMap.putIfAbsent(pageDesc.getMenuCategory(), descriptors);
		}
	}

	private void wireInPublicPages(ReverseRouteLookup reverseRouteLookup, Map<MenuCategory, List<SingleMenuItem>> publicMenuMap, BackendGuiDescriptor desc) {
		for(PageDescriptor pageDesc : desc.getWireIntoGuiDescriptors()) {
			if(pageDesc.isSecure())
				continue;
			
			List<SingleMenuItem> descriptors = publicMenuMap.getOrDefault(pageDesc.getMenuCategory(), new ArrayList<>());
			RouteMeta meta = reverseRouteLookup.get(pageDesc.getRouteId());
			if(meta.getRoute().getHttpMethod() != HttpMethod.GET)
				throw new RuntimeException("Plugin "+desc.getPluginName()+" supplied an illegal route id that is not a GET request="+pageDesc.getRouteId());
			
			String url = meta.getRoute().getFullPath();
			
			descriptors.add(new SingleMenuItem(pageDesc.getMenuTitle(), url));
			publicMenuMap.putIfAbsent(pageDesc.getMenuCategory(), descriptors);
		}
	}
	
	public synchronized Menu getMenu() {
		if(menu == null)
			createMenuOnce();
		
		return menu;
	}

	private void createMenuOnce() {
		Map<MenuCategory, List<SingleMenuItem>> secureMenuMap = new HashMap<>();
		Map<MenuCategory, List<SingleMenuItem>> publicMenuMap = new HashMap<>();

		for(BackendGuiDescriptor desc : descriptors) {
			wireInSecurePages(routes.getReverseRouteLookup(), secureMenuMap, desc);
			wireInPublicPages(routes.getReverseRouteLookup(), publicMenuMap, desc);
		}
		
		menu = new Menu(create(secureMenuMap), create(publicMenuMap));
	}

	private List<SingleMenu> create(Map<MenuCategory, List<SingleMenuItem>> secureMenuMap) {
		List<SingleMenu> menu = new ArrayList<>();

		for(Map.Entry<MenuCategory, List<SingleMenuItem>> entry : secureMenuMap.entrySet()) {
			menu.add(convert(entry));
		}
		return menu;
	}
	
}

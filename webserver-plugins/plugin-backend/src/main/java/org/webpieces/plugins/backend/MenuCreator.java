package org.webpieces.plugins.backend;

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

@Singleton
public class MenuCreator {
	
	private List<SingleMenu> menu = new ArrayList<>();

	@Inject
	public MenuCreator(Set<BackendGuiDescriptor> descriptors) {
		Map<MenuCategory, List<PageDescriptor>> menuMap = new HashMap<>();
		for(BackendGuiDescriptor desc : descriptors) {
			wireInPlugin(menuMap, desc);
		}
		
		for(Map.Entry<MenuCategory, List<PageDescriptor>> entry : menuMap.entrySet()) {
			menu.add(convert(entry));
		}
	}
	
	private SingleMenu convert(Entry<MenuCategory, List<PageDescriptor>> entry) {
		return new SingleMenu(entry.getKey(), entry.getValue());
	}
	
	private void wireInPlugin(Map<MenuCategory, List<PageDescriptor>> menuMap, BackendGuiDescriptor desc) {
		for(PageDescriptor pageDesc : desc.getWireIntoGuiDescriptors()) {
			List<PageDescriptor> descriptors = menuMap.getOrDefault(pageDesc.getMenuCategory(), new ArrayList<>());
			descriptors.add(pageDesc);
			menuMap.putIfAbsent(pageDesc.getMenuCategory(), descriptors);
		}
	}
	
	public List<SingleMenu> getMenu() {
		return menu;
	}
	
}

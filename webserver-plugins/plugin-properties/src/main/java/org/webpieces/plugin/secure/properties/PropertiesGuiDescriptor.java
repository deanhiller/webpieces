package org.webpieces.plugin.secure.properties;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugin.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugin.backend.spi.MenuCategory;
import org.webpieces.plugin.backend.spi.PageDescriptor;

public class PropertiesGuiDescriptor implements BackendGuiDescriptor {

	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.MANAGEMENT, "Modify Server Properties", PropertiesRouteId.MAIN_PROPERTIES, true));
		
		return descriptors;
	}

	@Override
	public String getPluginName() {
		return PropertiesPlugin.class.getName();
	}

}

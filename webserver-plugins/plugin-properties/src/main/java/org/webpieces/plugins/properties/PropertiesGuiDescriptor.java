package org.webpieces.plugins.properties;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;

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

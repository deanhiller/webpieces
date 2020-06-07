package org.webpieces.plugin.hsqldb;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugin.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugin.backend.spi.MenuCategory;
import org.webpieces.plugin.backend.spi.PageDescriptor;

public class H2DbGuiDescriptor implements BackendGuiDescriptor {

	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.MANAGEMENT, "InMemory Database", H2DbRouteId.REDIRECT_TO_DB_GUI, false));
		return descriptors;
	}
	
	@Override
	public String getPluginName() {
		return H2DbGuiDescriptor.class.getName();
	}

}

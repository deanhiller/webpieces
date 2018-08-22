package org.webpieces.plugins.hsqldb;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;

public class H2DbGuiDescriptor implements BackendGuiDescriptor {

	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.MANAGEMENT, "InMemory Database", H2DbRouteId.GET_DATABASE_PAGE));
		return descriptors;
	}
	
	@Override
	public String getPluginName() {
		return H2DbGuiDescriptor.class.getName();
	}

}

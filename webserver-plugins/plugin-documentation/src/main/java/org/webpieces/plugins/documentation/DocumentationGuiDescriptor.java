package org.webpieces.plugins.documentation;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;

public class DocumentationGuiDescriptor implements BackendGuiDescriptor {

	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Documentation Home", DocumentationRouteId.MAIN_DOCS, false));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Templates Reference", DocumentationRouteId.TEMPLATES, false));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Routes Reference", DocumentationRouteId.ROUTES, false));
		
		return descriptors;
	}

	@Override
	public String getPluginName() {
		return WebpiecesDocumentationPlugin.class.getName();
	}

}

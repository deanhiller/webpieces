package org.webpieces.plugin.documentation;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugin.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugin.backend.spi.MenuCategory;
import org.webpieces.plugin.backend.spi.PageDescriptor;

public class DocumentationGuiDescriptor implements BackendGuiDescriptor {

	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Documentation Home & Quick Start", DocumentationRouteId.MAIN_DOCS, false));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Widget Reference", DocumentationRouteId.HTML_REFERENCE, false));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Routes Reference", DocumentationRouteId.ROUTES, false));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Controllers Reference", DocumentationRouteId.CONTROLLERS, false));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Templates Reference", DocumentationRouteId.TEMPLATES, false));
		
		return descriptors;
	}

	@Override
	public String getPluginName() {
		return WebpiecesDocumentationPlugin.class.getName();
	}

}

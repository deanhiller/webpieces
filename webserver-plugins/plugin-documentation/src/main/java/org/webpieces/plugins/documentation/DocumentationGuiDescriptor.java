package org.webpieces.plugins.documentation;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;

public class DocumentationGuiDescriptor implements BackendGuiDescriptor {

	private String templates;
	private String home;
	private String routes;

	@Inject()
	public DocumentationGuiDescriptor(DocumentationConfig config) {
		String pathPrefix = config.getDocumentationPath();
		home = pathPrefix + DocumentationRoutes.HOME_PATH;
		templates = pathPrefix + DocumentationRoutes.TEMPLATES_PATH;
		routes = pathPrefix + DocumentationRoutes.ROUTES_PATH;
	}
	
	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Documentation Home", home));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Templates Reference", templates));
		descriptors.add(new PageDescriptor(MenuCategory.DOCUMENTATION, "Routes Reference", routes));
		
		return descriptors;
	}

}

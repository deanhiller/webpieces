package org.webpieces.plugins.sslcert;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugins.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugins.backend.spi.MenuCategory;
import org.webpieces.plugins.backend.spi.PageDescriptor;

public class InstallSslCertGuiDescriptor implements BackendGuiDescriptor {

	@Override
	public List<PageDescriptor> getWireIntoGuiDescriptors() {
		List<PageDescriptor> descriptors = new ArrayList<>();
		descriptors.add(new PageDescriptor(MenuCategory.MANAGEMENT, "SSL Cert Management", InstallSslCertRouteId.INSTALL_SSL_SETUP));
		return descriptors;
	}

	@Override
	public String getPluginName() {
		return InstallSslCertPlugin.class.getName();
	}

}

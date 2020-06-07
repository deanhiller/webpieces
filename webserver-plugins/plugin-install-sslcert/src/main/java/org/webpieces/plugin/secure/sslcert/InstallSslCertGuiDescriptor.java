package org.webpieces.plugin.secure.sslcert;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.plugin.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugin.backend.spi.MenuCategory;
import org.webpieces.plugin.backend.spi.PageDescriptor;

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

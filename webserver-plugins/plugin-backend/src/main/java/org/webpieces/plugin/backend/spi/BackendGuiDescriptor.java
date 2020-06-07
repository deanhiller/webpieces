package org.webpieces.plugin.backend.spi;

import java.util.List;

public interface BackendGuiDescriptor {

	public String getPluginName();
	
	public List<PageDescriptor> getWireIntoGuiDescriptors();

}

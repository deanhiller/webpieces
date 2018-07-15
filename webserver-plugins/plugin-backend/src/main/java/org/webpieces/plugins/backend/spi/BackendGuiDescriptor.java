package org.webpieces.plugins.backend.spi;

import java.util.List;

public interface BackendGuiDescriptor {

	public List<PageDescriptor> getWireIntoGuiDescriptors();

}

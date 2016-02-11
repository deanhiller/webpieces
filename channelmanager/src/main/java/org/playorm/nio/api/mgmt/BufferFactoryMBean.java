package org.playorm.nio.api.mgmt;

import biz.xsoftware.api.platform.mgmt.Documentation;

/**
 * Specific to the ChannelManagerService's BufferFactory implementation
 * 
 * @author dean.hiller
 */
public interface BufferFactoryMBean {

	public void setDirect(boolean b);

	@Documentation("The type of Buffer used for reading data from the socket.  Can be changed while running")
	public boolean isDirect();
	
}

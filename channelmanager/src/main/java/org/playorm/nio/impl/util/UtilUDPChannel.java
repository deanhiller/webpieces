package org.playorm.nio.impl.util;

import org.playorm.nio.api.channels.UDPChannel;


public class UtilUDPChannel extends UtilChannel implements UDPChannel {

	private UDPChannel realChannel;

	public UtilUDPChannel(UDPChannel realChannel) {
		super(realChannel);
		this.realChannel = realChannel;
	}

    /**
     * @see org.playorm.nio.api.channels.UDPChannel#disconnect()
     */
    public void disconnect()
    {
        realChannel.disconnect();
    }

	@Override
	public void oldClose() {
		realChannel.oldClose();
	}
}

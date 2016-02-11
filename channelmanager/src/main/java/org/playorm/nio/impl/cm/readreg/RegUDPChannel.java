package org.playorm.nio.impl.cm.readreg;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.UDPChannel;


public class RegUDPChannel extends RegHelperChannel implements UDPChannel {

	private static final Logger apiLog = Logger.getLogger(UDPChannel.class.getName());
	private UDPChannel realChannel;

	public RegUDPChannel(UDPChannel realChannel) {
		super(realChannel);
		this.realChannel = realChannel;
	}

	public synchronized void disconnect() {
		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"RegRead.registerForReads called");
		
		if(isRegistered) {
			realChannel.unregisterForReads();
			isRegistered = false;
		}
		realChannel.disconnect();
	}
}

package org.playorm.nio.api.libs;

import org.playorm.nio.api.channels.RegisterableChannel;

/**
 * This is the type of Runnable fed to the ExecutorService so if one 
 * implements their own ExecutorService, they can route the Runnable
 * to one of many threadpools based on type of Channel, or based on
 * hashCode so requests from the same client go to the same threads
 * 
 * @author dean.hiller
 *
 */
public interface ChannelsRunnable extends Runnable {
	
	public RegisterableChannel getChannel();
	
}

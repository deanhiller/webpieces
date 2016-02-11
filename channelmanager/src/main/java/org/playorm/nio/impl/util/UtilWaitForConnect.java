/**
 * 
 */
package org.playorm.nio.impl.util;

import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;


public class UtilWaitForConnect implements ConnectionCallback {

	private Throwable e;
	private boolean isFinished = false;
	
	public synchronized void connected(Channel channels) throws IOException {
		isFinished = true;
		this.notifyAll();
	}

	public synchronized void failed(RegisterableChannel channel, Throwable e) {
		this.e = e;
		isFinished = true;
		this.notifyAll();
	}
	
	public synchronized void waitForConnect() {
		try {
			if(!isFinished)
				this.wait();
		} catch(InterruptedException e) {
			throw new NioException(e);
		}
		
		if(e != null) {
			throw new NioException(e);
		}	
	}
}
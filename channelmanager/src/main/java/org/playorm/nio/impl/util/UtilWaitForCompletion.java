/**
 * 
 */
package org.playorm.nio.impl.util;

import java.io.IOException;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.handlers.OperationCallback;


public class UtilWaitForCompletion implements OperationCallback {

	private Throwable e;
	private boolean isFinished = false;
	private Channel channel;
	private Object thread;
	
	public UtilWaitForCompletion(Channel c, Object t) {
		channel = c;
		thread = t;
	}
	
	public synchronized void finished(Channel c) throws IOException {
		isFinished = true;
		this.notifyAll();
	}

	public synchronized void failed(RegisterableChannel c, Throwable e) {
		this.e = e;
		isFinished = true;
		this.notifyAll();
	}
	
	public synchronized void waitForComplete() throws IOException, InterruptedException {
		if(!isFinished)
			this.wait(30000);
		if(!isFinished)
			throw new RuntimeException(channel+"Failed to finish for 10 seconds.  current="+Thread.currentThread()+" selector="+thread);
		
		if(e != null) {
			if(e instanceof IOException)
				throw (IOException)e;
			else if(e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException(e);
		}	
	}
}
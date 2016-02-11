package org.playorm.nio.api.handlers;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;


public final class NullWriteCallback implements OperationCallback {

	public static final Logger log = Logger.getLogger(NullWriteCallback.class
			.getName());
	private static NullWriteCallback handler = new NullWriteCallback();
    private static final int MAX_PORT_UNREACHABLES = 10;
    
    private int numPortUnreachables = 0;
    
	private NullWriteCallback() {
	}
	
	public static NullWriteCallback singleton() {
		return handler;
	}
	
	public void finished(Channel c) throws IOException {
	}

	public void failed(RegisterableChannel c, Throwable e) {
    	if(e instanceof PortUnreachableException) {
    		numPortUnreachables++;
    		if(numPortUnreachables < MAX_PORT_UNREACHABLES)
    			return;
    		log.info("port unreachable exception(these " +
    				"are usually normal during phone setup/teardown)");
    	} else 
    		log.log(Level.WARNING, c + "Exceptoin on operation", e);		
	}

}

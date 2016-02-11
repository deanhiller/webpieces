package org.playorm.util.api.safethread;

import java.net.PortUnreachableException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 */
public final class NullExceptionListener implements ExceptionListener
{
    private static final Logger log = Logger.getLogger(NullExceptionListener.class.getName());
    private static final NullExceptionListener NULL_LISTENER = new NullExceptionListener();
    private static final int MAX_PORT_UNREACHABLES = 10;
    
    private int numPortUnreachables = 0;
    
    private NullExceptionListener() {}
    
    public void clearPortUnreachables() {
    	numPortUnreachables = 0;
    }
    
    public void fireFailure(Throwable e, Object chain)
    {
    	if(e instanceof PortUnreachableException) {
    		numPortUnreachables++;
    		if(numPortUnreachables < MAX_PORT_UNREACHABLES)
    			return;
    		log.info("port unreachable exception(these " +
    				"are usually normal during phone setup/teardown)");
    	} else 
    		log.log(Level.WARNING, chain + "Chain problem", e);
    }

    public static NullExceptionListener singleton() {
        return NULL_LISTENER;
    }
}

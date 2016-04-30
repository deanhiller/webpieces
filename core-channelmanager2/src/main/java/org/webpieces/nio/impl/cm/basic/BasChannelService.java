package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.nio.api.testutil.chanapi.ChannelsFactory;
import org.webpieces.nio.api.testutil.nioapi.SelectorProviderFactory;
import org.webpieces.nio.impl.cm.basic.udp.DatagramChannelImpl;
import org.webpieces.nio.impl.cm.basic.udp.UDPChannelImpl;

import com.webpieces.data.api.BufferPool;



/**
 * @author Dean Hiller
 */
class BasChannelService implements ChannelManager {

	private SelectorManager2 selMgr;
	private String objectId;
	private String cmId;
    private ChannelsFactory channelFactory;
	private boolean started;
	
	BasChannelService(String id, ChannelsFactory c, 
			SelectorProviderFactory mgr, BufferPool pool) {
		if(id == null || id.length() == 0)
			throw new IllegalArgumentException("id cannot be null");
		this.cmId = "["+id+"] ";
		
		selMgr = new SelectorManager2(mgr, cmId, pool);
		this.objectId = id;
        this.channelFactory = c;
        start();
	}
	
    public TCPServerChannel createTCPServerChannel(String id, ConnectionListener listener, DataListener dataListener) {
        preconditionChecks(id);
        if(dataListener == null)
        	throw new IllegalArgumentException("dataListener cannot be null");
        else if(listener == null)
        	throw new IllegalArgumentException("connectionListener cannot be null");
        IdObject obj = new IdObject(objectId, id);
        return new BasTCPServerChannel(obj, channelFactory, selMgr, listener, dataListener);
    }

	private void preconditionChecks(String id) {
		if(id == null)
            throw new IllegalArgumentException("id cannot be null");
		else if(!started)
			throw new IllegalStateException("Call start() on the ChannelManagerService first");
	}
	
    public TCPChannel createTCPChannel(String id, DataListener dataListener) {
        preconditionChecks(id);
        if(dataListener == null)
        	throw new IllegalArgumentException("dataListener cannot be null");
        IdObject obj = new IdObject(objectId, id);      
        return new BasTCPChannel(obj, channelFactory, selMgr, dataListener);
    } 

    public UDPChannel createUDPChannel(String id, DataListener dataListener) {
        preconditionChecks(id);
        if(dataListener == null)
        	throw new IllegalArgumentException("dataListener cannot be null");
        IdObject obj = new IdObject(objectId, id);
        return new UDPChannelImpl(obj, selMgr, dataListener);
    }
    
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener dataListener) {
        if(dataListener == null)
        	throw new IllegalArgumentException("dataListener cannot be null");
        return new DatagramChannelImpl(id, bufferSize, dataListener);
    }
    
	public void start() {
		started = true;
		selMgr.start();
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManager#shutdown()
	 */
	public void stop() {
		started = false;
		selMgr.stop();
	}
	
	public String toString() {
		return cmId;
	}
}

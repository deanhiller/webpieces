package org.webpieces.nio.impl.cm.basic;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.deprecated.ChannelService;
import org.webpieces.nio.api.testutil.chanapi.ChannelsFactory;
import org.webpieces.nio.api.testutil.nioapi.SelectorProviderFactory;
import org.webpieces.nio.impl.cm.basic.udp.DatagramChannelImpl;
import org.webpieces.nio.impl.cm.basic.udp.UDPChannelImpl;



/**
 * @author Dean Hiller
 */
class BasChannelService implements ChannelService {

	private SelectorManager2 selMgr;
	private String objectId;
	private String cmId;
    private ChannelsFactory channelFactory;
	private boolean started;
	
	BasChannelService(String id, ChannelsFactory c, SelectorProviderFactory mgr, BufferPool pool) {
		if(id == null || id.length() == 0)
			throw new IllegalArgumentException("id cannot be null");
		this.cmId = "["+id+"] ";
		
		selMgr = new SelectorManager2(mgr, cmId, pool);
		this.objectId = id;
        this.channelFactory = c;
	}
	
    public TCPServerChannel createTCPServerChannel(String id) {
        preconditionChecks(id);
        IdObject obj = new IdObject(objectId, id);
        return new BasTCPServerChannel(obj, channelFactory, selMgr);
    }

	private void preconditionChecks(String id) {
		if(id == null)
            throw new IllegalArgumentException("id cannot be null");
		else if(!started)
			throw new IllegalStateException("Call start() on the ChannelManagerService first");
	}
	
    public TCPChannel createTCPChannel(String id) {
        preconditionChecks(id);        
        IdObject obj = new IdObject(objectId, id);      
        return new BasTCPChannel(obj, channelFactory, selMgr);
    } 

    public UDPChannel createUDPChannel(String id) {
        preconditionChecks(id);
        IdObject obj = new IdObject(objectId, id);
        return new UDPChannelImpl(obj, selMgr);
    }
    
	public DatagramChannel createDatagramChannel(String id, int bufferSize) {
        return new DatagramChannelImpl(id, bufferSize);
    }
    
	public void start() {
		started = true;
		selMgr.start();
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManager#shutdown()
	 */
	public void stop() {
		selMgr.stop();
	}
	
	public String toString() {
		return cmId;
	}
}

package org.playorm.nio.impl.cm.basic;

import java.io.IOException;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.testutil.chanapi.ChannelsFactory;
import org.playorm.nio.api.testutil.nioapi.SelectorProviderFactory;
import org.playorm.nio.impl.cm.basic.udp.DatagramChannelImpl;
import org.playorm.nio.impl.cm.basic.udp.UDPChannelImpl;



/**
 * @author Dean Hiller
 */
class BasChannelService implements ChannelService {

	private SelectorManager2 selMgr;
	private String objectId;
	private String cmId;
	private BufferFactory bufFactory;
    private ChannelsFactory channelFactory;
	private boolean started;
	
	BasChannelService(String id, ChannelsFactory c, SelectorProviderFactory mgr, BufferFactory bufFactory) {
		if(id == null || id.length() == 0)
			throw new IllegalArgumentException("id cannot be null");
		this.cmId = "["+id+"] ";
		
		selMgr = new SelectorManager2(mgr, cmId);
		this.objectId = id;
		this.bufFactory = bufFactory;
        this.channelFactory = c;
	}
	
    public TCPServerChannel createTCPServerChannel(String id, Settings h) throws IOException {
        preconditionChecks(id);
        IdObject obj = new IdObject(objectId, id);
        return new BasTCPServerChannel(obj, channelFactory, bufFactory, selMgr);
    }

	private void preconditionChecks(String id) {
		if(id == null)
            throw new IllegalArgumentException("id cannot be null");
		else if(!started)
			throw new IllegalStateException("Call start() on the ChannelManagerService first");
	}
	
    public TCPChannel createTCPChannel(String id, Settings h) throws IOException {
        preconditionChecks(id);        
        IdObject obj = new IdObject(objectId, id);      
        return new BasTCPChannel(obj, channelFactory, bufFactory, selMgr);
    } 

    public UDPChannel createUDPChannel(String id, Settings h) throws IOException {
        preconditionChecks(id);
        IdObject obj = new IdObject(objectId, id);
        return new UDPChannelImpl(obj, bufFactory, selMgr);
    }
    
	public DatagramChannel createDatagramChannel(String id, int bufferSize) throws IOException {
        return new DatagramChannelImpl(id, bufferSize);
    }
    
	public void start() throws IOException {
		started = true;
		selMgr.start();
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ChannelManager#shutdown()
	 */
	public void stop() throws IOException, InterruptedException {
		selMgr.stop();
	}
	
	public String toString() {
		return cmId;
	}
}

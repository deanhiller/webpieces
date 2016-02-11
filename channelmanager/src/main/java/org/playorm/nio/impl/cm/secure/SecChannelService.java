package org.playorm.nio.impl.cm.secure;

import java.io.IOException;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.impl.util.UtilProxyTCPChannel;
import org.playorm.nio.impl.util.UtilProxyTCPServerChannel;
import org.playorm.nio.impl.util.UtilUDPChannel;



/**
 * @author Dean Hiller
 */
class SecChannelService implements ChannelService {


	private ChannelService mgr;

	public SecChannelService(String id, ChannelService manager) {
		this.mgr = manager;
	}

    public TCPServerChannel createTCPServerChannel(String id, Settings h) throws IOException {
        TCPServerChannel channel = mgr.createTCPServerChannel(id, h);
        if(h == null || h.getSSLEngineFactory() == null) {
            return new UtilProxyTCPServerChannel(channel);
        }
        return new SecTCPServerChannel(channel, h.getSSLEngineFactory());
    }

    public TCPChannel createTCPChannel(String id, Settings h) throws IOException {
        TCPChannel realChannel = mgr.createTCPChannel(id, h);
        if(h == null || h.getSSLEngineFactory() == null) {
            return new UtilProxyTCPChannel(realChannel);
        }
        return new SecTCPChannel(realChannel, h.getSSLEngineFactory());
    } 

    public UDPChannel createUDPChannel(String id, Settings h) throws IOException {
        UDPChannel realChannel = mgr.createUDPChannel(id, h);
        UDPChannel channel = new UtilUDPChannel(realChannel);
        return channel;
    }

    public DatagramChannel createDatagramChannel(String id, int bufferSize) throws IOException {
        return mgr.createDatagramChannel(id, bufferSize);
    }
    
	public void start() throws IOException {
		mgr.start();
	}

	public void stop() throws IOException, InterruptedException {
		mgr.stop();
	}
	
	public String toString() {
		return mgr.toString();
	}


}

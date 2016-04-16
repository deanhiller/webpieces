package org.webpieces.nio.impl.cm.exception;

import java.io.IOException;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.deprecated.ChannelService;
import org.webpieces.nio.api.deprecated.Settings;
import org.webpieces.nio.impl.util.UtilUDPChannel;



/**
 * @author Dean Hiller
 */
class ExcChannelService implements ChannelService {


	private ChannelService mgr;

	public ExcChannelService(Object id, ChannelService manager) {
		this.mgr = manager;
	}

    public TCPServerChannel createTCPServerChannel(String id, Settings h) throws IOException {
        TCPServerChannel channel = mgr.createTCPServerChannel(id, h);
        return new ExcTCPServerChannel(channel);
    }

    public TCPChannel createTCPChannel(String id, Settings h) throws IOException {
        TCPChannel realChannel = mgr.createTCPChannel(id, h);
        ExcTCPChannel channel = new ExcTCPChannel(realChannel);
        return channel;
    } 

    public UDPChannel createUDPChannel(String id, Settings h) throws IOException {
        //TODO: implement this correctly.....
        UDPChannel realChannel = mgr.createUDPChannel(id, h);
        UDPChannel channel = new UtilUDPChannel(realChannel);
        return channel;
    }

    public DatagramChannel createDatagramChannel(String id, int bufferSize) throws IOException {
        //TODO: implement this correctly....
        return mgr.createDatagramChannel(id, bufferSize);
    }
    
	public void start() throws IOException {
		mgr.start();
	}

	public void stop() throws IOException, InterruptedException {
		mgr.stop();
	}


}

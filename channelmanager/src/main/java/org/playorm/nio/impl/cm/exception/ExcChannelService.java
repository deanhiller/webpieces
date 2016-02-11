package org.playorm.nio.impl.cm.exception;

import java.io.IOException;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.impl.util.UtilUDPChannel;



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

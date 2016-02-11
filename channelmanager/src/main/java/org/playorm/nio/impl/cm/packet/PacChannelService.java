package org.playorm.nio.impl.cm.packet;

import java.io.IOException;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.PacketProcessor;
import org.playorm.nio.impl.util.UtilProxyTCPChannel;
import org.playorm.nio.impl.util.UtilProxyTCPServerChannel;
import org.playorm.nio.impl.util.UtilUDPChannel;



/**
 * @author Dean Hiller
 */
class PacChannelService implements ChannelService {


	private ChannelService mgr;

	public PacChannelService(Object id, ChannelService manager) {
		this.mgr = manager;
	}

    public TCPServerChannel createTCPServerChannel(String id, Settings h) throws IOException {
        TCPServerChannel channel = mgr.createTCPServerChannel(id, h);
        if(h == null || h.getPacketProcessorFactory() == null) {
            return new UtilProxyTCPServerChannel(channel);
        }
        return new PacTCPServerChannel(channel, h.getPacketProcessorFactory());
    }

    public TCPChannel createTCPChannel(String id, Settings h) throws IOException {
        TCPChannel realChannel = mgr.createTCPChannel(id, h);
        if(h == null || h.getPacketProcessorFactory() == null) {
            return new UtilProxyTCPChannel(realChannel);
        }

        PacketProcessor processor = h.getPacketProcessorFactory().createPacketProcessor(realChannel);
        return new PacTCPChannel(realChannel, processor);
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


}

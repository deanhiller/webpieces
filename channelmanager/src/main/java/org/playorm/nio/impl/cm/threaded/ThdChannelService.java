package org.playorm.nio.impl.cm.threaded;

import java.io.IOException;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.impl.util.UtilUDPChannel;



/**
 * @author Dean Hiller
 */
class ThdChannelService implements ChannelService {


	private ChannelService mgr;
	private SpecialExecutor svc;
	private Object id;
	private BufferFactory bufFactory;

	public ThdChannelService(Object id, ChannelService manager, SpecialExecutor executorFactory, BufferFactory bufFactory) {
		this.id = id;
		this.mgr = manager;
		this.svc = executorFactory;
		this.bufFactory = bufFactory;
	}

    public TCPServerChannel createTCPServerChannel(String id, Settings h) throws IOException {
        TCPServerChannel channel = mgr.createTCPServerChannel(id, h);
        return new ThdTCPServerChannel(channel, svc, bufFactory);
    }

    public TCPChannel createTCPChannel(String id, Settings h) throws IOException {
        TCPChannel realChannel = mgr.createTCPChannel(id, h);
        ThdTCPChannel channel = new ThdTCPChannel(realChannel, svc, bufFactory);
        return channel;
    } 

    public UDPChannel createUDPChannel(String id, Settings h) throws IOException {
        //TODO: implement this correctly....
        UDPChannel realChannel = mgr.createUDPChannel(id, h);
        UDPChannel channel = new UtilUDPChannel(realChannel);
        return channel;
    }

    public DatagramChannel createDatagramChannel(String id, int bufferSize) throws IOException {
        //TODO: implement this correctly....
        return mgr.createDatagramChannel(id, bufferSize);
    }
    
	public void start() throws IOException {
		svc.start(id);
		mgr.start();
	}

	public void stop() throws IOException, InterruptedException {
		//stop the manager first so it doesn't feed more tasks to the
		//executor service and get errors...
		mgr.stop();
		//now this stinks...ChannelManager is shutdown, but the threads might feed just got 
		//connected tasks(that are still outstanding/running) in which might try to registerForReads on connection which will throw
		//exceptions since the channelmanager is shutdown!
		//BIG NOTE: We cannot call plain shutdown as that task executes all the tasks in the queue
		//and there is a good chance those call back to the channelmanager and will just result in 
		//exceptions
		svc.stop(id);
	}

//	public void stopNow() throws IOException, InterruptedException {
//		svc.shutdownNow();
//		svc = null;
//		mgr.stop();
//	}

}

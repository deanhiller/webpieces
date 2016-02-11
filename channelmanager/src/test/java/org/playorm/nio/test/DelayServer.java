package org.playorm.nio.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.FactoryCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A delay server to introduce simulated network delays for testing
 * performance of channelmanager.  It is a simple pass through but purposely
 * caches the data and sends it x milliseconds later.
 * 
 * @author dean.hiller
 */
public class DelayServer {
	
	private static final Logger log = LoggerFactory.getLogger(DelayServer.class);

//	private static final Logger log = Logger.getLogger(MockNIOServer.class.getName());
	private ChannelService serverSideChanMgr;
	private ChannelService clientSideChanMgr;	
	private TCPServerChannel srvrChannel;
	private DelayServerAcceptor acceptor;
	
	public DelayServer() {
		ChannelServiceFactory factory = ChannelServiceFactory.createFactory(null);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		FactoryCreator creator = FactoryCreator.createFactory(null);
		BufferFactory bufFactory = creator.createBufferFactory(map);		
		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "[serverSide]");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);
		
		this.serverSideChanMgr = factory.createChannelManager(p);
		Map<String, Object> p2 = new HashMap<String, Object>();
		p2.put(ChannelManager.KEY_ID, "[clientSide]");
		p2.put(ChannelManager.KEY_BUFFER_FACTORY, bufFactory);		
		this.clientSideChanMgr = factory.createChannelManager(p2);
	}
	
	public InetSocketAddress start(InetSocketAddress realSvr) throws IOException, InterruptedException {
		int port = 0;
	    log.info("Starting server");
		clientSideChanMgr.start();
		serverSideChanMgr.start();
		InetAddress loopBack = InetAddress.getByName("127.0.0.1");
		
		acceptor = new DelayServerAcceptor(clientSideChanMgr, loopBack, realSvr);
		InetSocketAddress svrAddr = new InetSocketAddress(loopBack, port);		
		srvrChannel = serverSideChanMgr.createTCPServerChannel("ProxySvrChannel", null);
		srvrChannel.setReuseAddress(true);
		srvrChannel.bind(svrAddr);	
		srvrChannel.registerServerSocketChannel(acceptor);
		
		return srvrChannel.getLocalAddress();
	}
	
	public void stop() throws IOException, InterruptedException {		
		srvrChannel.oldClose();
		acceptor.closeAllSockets();
		
		serverSideChanMgr.stop();		
	}

//	public static void main(String[] args) throws Exception {

//		if(args.length < 2) {
//		 return;
//		}
//		
//		String realServerIp = args[0];
//		int port = Integer.parseInt(args[1]);
//		InetAddress addr = InetAddress.getByName(realServerIp);
//		InetSocketAddress svrAddr = new InetSocketAddress(addr, port);
//		DelayServer svr = new DelayServer();
//		svr.start(svrAddr);		
//	}
}

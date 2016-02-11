package org.playorm.nio.test.tcp;

import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.libs.SSLEngineFactory;
import org.playorm.nio.api.libs.StartableExecutorService;
import org.playorm.nio.api.testutil.MockSSLEngineFactory;


public class PerfTestZSecure extends ZPerformanceSuper {
	
	private ChannelServiceFactory secureFactory;
	private SSLEngineFactory sslEngineFactory;
	private Settings clientFactoryHolder;
	private Settings serverFactoryHolder;
    private StartableExecutorService clientExecFactory;
    private StartableExecutorService serverExecFactory;
	
	public PerfTestZSecure(String name) {
		super(name);
        FactoryCreator creator = FactoryCreator.createFactory(null);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(FactoryCreator.KEY_NUM_THREADS, 1);
        clientExecFactory = creator.createExecSvcFactory(map);
        serverExecFactory = creator.createExecSvcFactory(map);
        
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);

		Map<String, Object> threadedProps = new HashMap<String, Object>();
		threadedProps.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_THREAD_CHANNEL_MGR);
		threadedProps.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory threaded = ChannelServiceFactory.createFactory(threadedProps);
		
		Map<String, Object> factoryName = new HashMap<String, Object>();
		factoryName.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_SECURE_CHANNEL_MGR);
		factoryName.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, threaded);
		ChannelServiceFactory sslLayer = ChannelServiceFactory.createFactory(factoryName);
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
		props.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, sslLayer);
		secureFactory = ChannelServiceFactory.createFactory(props);	
		
		sslEngineFactory = new MockSSLEngineFactory();
		
		PacketProcessorFactory procFactory = creator.createPacketProcFactory(null);
		clientFactoryHolder = new Settings(sslEngineFactory, procFactory);
		serverFactoryHolder = new Settings(sslEngineFactory, procFactory);		
	}
	
	@Override
	protected ChannelService getClientChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "client");		
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
		p.put(ChannelManager.KEY_EXECUTORSVC_FACTORY, clientExecFactory);
		ChannelService chanMgr = secureFactory.createChannelManager(p);		
		return chanMgr;
	}

	@Override
	protected ChannelService getServerChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "echoServer");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
		p.put(ChannelManager.KEY_EXECUTORSVC_FACTORY, serverExecFactory);
		ChannelService svcChanMgr = secureFactory.createChannelManager(p);
		
		return svcChanMgr;
	}

	@Override
	protected Settings getClientFactoryHolder() {
		return clientFactoryHolder;
	}
	@Override
	protected Settings getServerFactoryHolder() {
		return serverFactoryHolder;
	}
	
	@Override
	protected String getChannelImplName() {
		return "biz.xsoftware.impl.nio.cm.secure.SecTCPChannel";
	}

	@Override
	protected int getBasicConnectTimeLimit() {
		return 300;
	}	
	
	/**
	 * Realize this limit is bigger when useing AsynchSSLEngineSynchronized
	 */
	@Override
	protected int getSmallReadWriteTimeLimit() {
		return 80;
	}

	@Override
	protected int getLargerReadWriteTimeLimit() {
		return 300;
	}

	
}

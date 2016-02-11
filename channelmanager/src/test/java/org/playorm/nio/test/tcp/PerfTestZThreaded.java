package org.playorm.nio.test.tcp;

import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;
import org.playorm.nio.api.libs.StartableExecutorService;


public class PerfTestZThreaded extends ZPerformanceSuper {

	private ChannelServiceFactory factory;
	private Settings factoryHolder;
    private StartableExecutorService clientExecFactory;
    private StartableExecutorService serverExecFactory;
	
	public PerfTestZThreaded(String name) {
		super(name);
        FactoryCreator creator = FactoryCreator.createFactory(null);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(FactoryCreator.KEY_NUM_THREADS, 1);
        clientExecFactory = creator.createExecSvcFactory(map);
        serverExecFactory = creator.createExecSvcFactory(map);
        
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
		props.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory packetFactory = ChannelServiceFactory.createFactory(props);
		
        Map<String, Object> props2 = new HashMap<String, Object>();
        props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_THREAD_CHANNEL_MGR);
        props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, packetFactory);
        ChannelServiceFactory threadedFactory = ChannelServiceFactory.createFactory(props2);
        
		Map<String, Object> props3 = new HashMap<String, Object>();
		props3.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
		props3.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, threadedFactory);
		factory = ChannelServiceFactory.createFactory(props3);
		
		Map<String, Object> settings = new HashMap<String, Object>();
		PacketProcessorFactory procFactory = creator.createPacketProcFactory(settings);		
		factoryHolder = new Settings(null, procFactory);		
	}
	
	@Override
	protected ChannelService getClientChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "client");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
        p.put(ChannelManager.KEY_EXECUTORSVC_FACTORY, clientExecFactory);
		return factory.createChannelManager(p);
	}

	@Override
	protected ChannelService getServerChanMgr() {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "server");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());		
        p.put(ChannelManager.KEY_EXECUTORSVC_FACTORY, serverExecFactory);
		return factory.createChannelManager(p);
	}

	@Override
	protected Settings getClientFactoryHolder() {
		return factoryHolder;
	}
	@Override
	protected Settings getServerFactoryHolder() {
		return factoryHolder;
	}
	
	@Override
	protected String getChannelImplName() {
		return "biz.xsoftware.impl.nio.cm.exception.ExcTCPChannel";
	}

	@Override
	protected int getBasicConnectTimeLimit() {
		return 10;
	}
	
	@Override
	protected int getSmallReadWriteTimeLimit() {
		return 100;
	}
	
	@Override
	protected int getLargerReadWriteTimeLimit() {
		return 90;
	}

    /**
     * @see org.playorm.nio.test.tcp.ZPerformanceSuper#testVerySmallReadWrite()
     */
    @Override
    public void testVerySmallReadWrite() throws Exception
    {
        super.testVerySmallReadWrite();
    }
	

}

package org.playorm.nio.test.tcp;

import java.util.HashMap;
import java.util.Map;

import org.playorm.nio.api.deprecated.ChannelManager;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.deprecated.Settings;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.PacketProcessorFactory;


public class PerfTestZBasic extends ZPerformanceSuper {

	private ChannelServiceFactory factory;
	private Settings factoryHolder;
	
	public PerfTestZBasic(String name) {
		super(name);
		ChannelServiceFactory basic = ChannelServiceFactory.createFactory(null);
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_PACKET_CHANNEL_MGR);
		props.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, basic);
		ChannelServiceFactory packetFactory = ChannelServiceFactory.createFactory(props);
		
		Map<String, Object> props2 = new HashMap<String, Object>();
		props2.put(ChannelServiceFactory.KEY_IMPLEMENTATION_CLASS, ChannelServiceFactory.VAL_EXCEPTION_CHANNEL_MGR);
		props2.put(ChannelServiceFactory.KEY_CHILD_CHANNELMGR_FACTORY, packetFactory);
		factory = ChannelServiceFactory.createFactory(props2);
		
		Map<String, Object> settings = new HashMap<String, Object>();	
		FactoryCreator creator = FactoryCreator.createFactory(null);
		PacketProcessorFactory procFactory = creator.createPacketProcFactory(settings);		
		factoryHolder = new Settings(null, procFactory);		
	}
	
	@Override
	protected ChannelService getClientChanMgr() {
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "client");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());
		return factory.createChannelManager(p);
	}

	@Override
	protected ChannelService getServerChanMgr() {		
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(ChannelManager.KEY_ID, "server");
		p.put(ChannelManager.KEY_BUFFER_FACTORY, getBufFactory());			
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

    @Override
    public void testLargeReadWrite() throws Exception
    {
        super.testLargeReadWrite();
    }
	

}

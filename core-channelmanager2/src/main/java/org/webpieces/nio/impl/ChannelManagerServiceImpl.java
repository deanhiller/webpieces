package org.webpieces.nio.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.deprecated.ChannelManagerOld;
import org.webpieces.nio.api.deprecated.ChannelService;
import org.webpieces.nio.api.deprecated.ChannelServiceFactory;
import org.webpieces.nio.api.libs.BufferFactory;
import org.webpieces.nio.api.libs.FactoryCreator;
import org.webpieces.nio.api.libs.StartableExecutorService;


/**
 * TODO: Try to move this class to impl packages without breaking the design and without
 * breaking osgi.  This should be doable!!!!!!!
 * 
 */
public class ChannelManagerServiceImpl implements ChannelService {

	private static final Logger log = Logger.getLogger(ChannelManagerServiceImpl.class.getName());
	private static final NotificationListener NULL_LIST = new NullNotifyListener();
	private ChannelService mgr;
	
	private NotificationListener notifyList = NULL_LIST;
	
	public ChannelManagerServiceImpl() {
		log.info("constructed object");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FactoryCreator.KEY_IS_DIRECT, false);
		map.put(FactoryCreator.KEY_NUM_THREADS, 10);		

		FactoryCreator creator = FactoryCreator.createFactory(null);		
		BufferFactory bufferFactory = creator.createBufferFactory(map);
		StartableExecutorService execSvcFactory = creator.createExecSvcFactory(map);
		mgr = createChannelManager(bufferFactory, execSvcFactory);		
	}

    public TCPServerChannel createTCPServerChannel(String id) {
        TCPServerChannel chan = mgr.createTCPServerChannel(id);
        notifyList.handleNotification(new Notification("register", this, 0), chan);
        return chan;
    }

    public TCPChannel createTCPChannel(String id) {
        TCPChannel chan = mgr.createTCPChannel(id);
        notifyList.handleNotification(new Notification("register", this, 0), chan);
        return chan;        
    } 
    
    public UDPChannel createUDPChannel(String id) {
        UDPChannel chan = mgr.createUDPChannel(id);
        notifyList.handleNotification(new Notification("register", this, 0), chan);
        return chan;
    }
    
    public DatagramChannel createDatagramChannel(String id, int bufferSize) {
        DatagramChannel chan = mgr.createDatagramChannel(id, bufferSize);
        notifyList.handleNotification(new Notification("register", this, 0), chan);
        return chan;
    }

	public void start() {
		log.info("Starting ChannelManager component");
		mgr.start();
		log.info("Started ChannelManager component");
	}

	public void stop() {
		log.info("Stopping ChannelManager component");
		mgr.stop();
		log.info("Stopped ChannelManager component");
	}

	public void setNotificationListener(NotificationListener l) {
		if(l == null)
			notifyList = NULL_LIST;
		else
			notifyList = l;
	}
	
	private static class NullNotifyListener implements NotificationListener {
		public void handleNotification(Notification notification, Object handback) {
		}
	}
	
	protected ChannelService createChannelManager(BufferFactory bufferFactory, StartableExecutorService execSvcFactory) {
		
		ChannelServiceFactory factory = ChannelServiceFactory.createDefaultStack();
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(ChannelManagerOld.KEY_ID, "ChanMgr");
		props.put(ChannelManagerOld.KEY_EXECUTORSVC_FACTORY, execSvcFactory);
		props.put(ChannelManagerOld.KEY_BUFFER_FACTORY, bufferFactory);
		ChannelService mgr = factory.createChannelManager(props);
		return mgr;
	}

}

package org.webpieces.nio.api;

import java.util.Map;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.deprecated.ChannelService;
import org.webpieces.nio.api.deprecated.ChannelServiceFactory;


public class ChannelManagerFactory {

	private ChannelManagerFactory() {}
	
	public static ChannelManager createChannelManager(String id, Map<String, Object> props) {
		ChannelServiceFactory factory = ChannelServiceFactory.createFactory(null);
		ChannelService svc = factory.createChannelManager(null);
		svc.start();
		return new ChannelMgrProxy(svc);
	}
	
	private static class ChannelMgrProxy implements ChannelManager {

		private ChannelService svc;

		public ChannelMgrProxy(ChannelService svc) {
			this.svc = svc;
		}

		@Override
		public TCPServerChannel createTCPServerChannel(String id) {
			return svc.createTCPServerChannel(id);
		}
		
		@Override
		public TCPChannel createTCPChannel(String id) {
			return svc.createTCPChannel(id);
		}

		@Override
		public UDPChannel createUDPChannel(String id) {
			return svc.createUDPChannel(id);
		}

		@Override
		public DatagramChannel createDatagramChannel(String id, int bufferSize) {
			return svc.createDatagramChannel(id, bufferSize);
		}

		@Override
		public void stop() {
			svc.stop();
		}
	}
}

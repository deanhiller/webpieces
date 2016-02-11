package org.playorm.nio.api;

import java.io.IOException;
import java.util.Map;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;
import org.playorm.nio.api.deprecated.ChannelService;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;


public class ChannelManagerFactory {

	private ChannelManagerFactory() {}
	
	public static ChannelManager createChannelManager(String id, Map<String, Object> props) {
		ChannelService svc = ChannelServiceFactory.createNewChannelManager(id);
		try {
			svc.start();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return new ChannelMgrProxy(svc);
	}
	
	private static class ChannelMgrProxy implements ChannelManager {

		private ChannelService svc;

		public ChannelMgrProxy(ChannelService svc) {
			this.svc = svc;
		}

		@Override
		public TCPServerChannel createTCPServerChannel(String id) {
			try {
				return svc.createTCPServerChannel(id, null);
			} catch (IOException e) {
				throw new NioException(e);
			}
		}

		@Override
		public TCPChannel createTCPChannel(String id) {
			try {
				return svc.createTCPChannel(id, null);
			} catch (IOException e) {
				throw new NioException(e);
			}
		}

		@Override
		public UDPChannel createUDPChannel(String id) {
			try {
				return svc.createUDPChannel(id, null);
			} catch (IOException e) {
				throw new NioException(e);
			}
		}

		@Override
		public DatagramChannel createDatagramChannel(String id, int bufferSize) {
			try {
				return svc.createDatagramChannel(id, bufferSize);
			} catch (IOException e) {
				throw new NioException(e);
			}
		}

		@Override
		public void stop() {
			try {
				svc.stop();
			} catch (IOException e) {
				throw new NioException(e);
			} catch (InterruptedException e) {
				throw new NioException(e);
			}
		}
	}
}

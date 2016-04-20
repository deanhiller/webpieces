package org.webpieces.httpproxy.api;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.futures.Future;

public class MockTcpChannel implements TCPChannel {

	private DataListener dataListener;
	private ChannelSession session = new MyChanSession();

	@Override
	public Future<Channel, FailureInfo> connect(SocketAddress addr) {
		return null;
	}

	@Override
	public Future<Channel, FailureInfo> write(ByteBuffer b) {
		return null;
	}

	@Override
	public Future<Channel, FailureInfo> close() {
		return null;
	}

	@Override
	public void registerForReads(DataListener listener) {
		this.dataListener = listener;
	}

	@Override
	public void unregisterForReads() {
		
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void setReuseAddress(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bind(SocketAddress addr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBlocking() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public DataListener getDataListener() {
		return dataListener;
	}

	@Override
	public ChannelSession getSession() {
		return session ;
	}
	
	private static class MyChanSession implements ChannelSession {
		private Map<Object, Object> props = new HashMap<>();

		@Override
		public void put(Object key, Object value) {
			props.put(key, value);
		}

		@Override
		public Object get(Object key) {
			return props.get(key);
		}
		
	}

	@Override
	public void setWriteTimeoutMs(int timeout) {
	}

	@Override
	public int getWriteTimeoutMs() {
		return 0;
	}

}

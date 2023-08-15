package org.webpieces.httpfrontend2.api.mock2;

import java.util.List;

import javax.net.ssl.SSLEngine;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.Throttle;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;

public class MockChanMgr extends MockSuperclass implements ChannelManager {

	private enum Method implements MethodEnum {
		CREATE_TCP_SVR_CHANNEL
	}
	
	public void addTCPSvrChannelToReturn(TCPServerChannel toReturn) {
		super.addValueToReturn(Method.CREATE_TCP_SVR_CHANNEL, toReturn);
	}
	
	public ConnectionListener getSingleConnectionListener() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.CREATE_TCP_SVR_CHANNEL);
		if(list.size() != 1)
			throw new IllegalStateException("expected exactly one listener but there were="+list.size());
		return (ConnectionListener) list.get(0).getArgs()[1];
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener) {
		return (TCPServerChannel) super.calledMethod(Method.CREATE_TCP_SVR_CHANNEL, id, connectionListener);
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public TCPChannel createTCPChannel(String id, SSLEngine engine) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public void stop() {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public String getName() {
		return "mockChanMgr";
	}

	@Override
	public TCPServerChannel createTCPUpgradableChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	@Override
	public Throttle getThrottle() {
		throw new UnsupportedOperationException("not supported yet");
	}
}

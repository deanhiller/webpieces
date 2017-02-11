package org.webpieces.httpfrontend2.api.mock;

import java.util.stream.Stream;

import javax.net.ssl.SSLEngine;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;

public class MockChannelManager extends MockSuperclass implements ChannelManager {

	private static enum Method implements MethodEnum {
		CREATE_TCP_SERVER_CHANNEL, CREATE_SSL_SERVER_CHANNEL;
	}
	
	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener) {
		return (TCPServerChannel) super.calledMethod(Method.CREATE_TCP_SERVER_CHANNEL, id, connectionListener);
	}

	@Override
	public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener,
			SSLEngineFactory factory) {
		return (TCPServerChannel) super.calledMethod(Method.CREATE_TCP_SERVER_CHANNEL, id, connectionListener, factory);
	}

	@Override
	public TCPChannel createTCPChannel(String id) {
		return null;
	}

	@Override
	public TCPChannel createTCPChannel(String id, SSLEngine engine) {
		return null;
	}

	@Override
	public UDPChannel createUDPChannel(String id) {
		return null;
	}

	@Override
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener) {
		return null;
	}

	@Override
	public void stop() {
	}

	public void addTcpSvrChannel(MockTcpServerChannel mockChannel) {
		super.addValueToReturn(Method.CREATE_TCP_SERVER_CHANNEL, mockChannel);
	}

	public ConnectionListener[] fetchTcpConnectionListeners() {
		Stream<ParametersPassedIn> calledMethods = super.getCalledMethods(Method.CREATE_TCP_SERVER_CHANNEL);
		Stream<ConnectionListener> retVal = calledMethods.map(p -> (ConnectionListener)p.getArgs()[1]);
		return retVal.toArray(ConnectionListener[]::new);
	}

	public ConnectionListener getConnListener() {
		ConnectionListener[] listeners = fetchTcpConnectionListeners();
		if(listeners.length != 1)
			throw new IllegalStateException("listeners not exactly 1.  size="+listeners.length);
		return listeners[0];
	}

}

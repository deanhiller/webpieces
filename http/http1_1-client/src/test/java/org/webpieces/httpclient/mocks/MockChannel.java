package org.webpieces.httpclient.mocks;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MockChannel extends MockSuperclass implements TCPChannel {
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpStatefulParser parser;
	private DataListener listener;
	private boolean isClosed;

	private enum Method implements MethodEnum {
		CONNECT, WRITE
	}

	public MockChannel() {
		parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		if(this.listener != null)
			throw new IllegalStateException("connect should only be called once");
		this.listener = listener;
		return (XFuture<Void>) super.calledMethod(Method.CONNECT, addr, listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> write(ByteBuffer b) {
		DataWrapper wrapper = dataGen.wrapByteBuffer(b);
		List<HttpPayload> parsedData = parser.parse(wrapper);
		if(parsedData.size() != 1)
			throw new IllegalArgumentException("The impl should be writing out full single payloads each write call");
		HttpPayload payload = parsedData.get(0);
		return (XFuture<Void>) super.calledMethod(Method.WRITE, payload);
	}

	@Override
	public XFuture<Void> close() {
		isClosed = true;
		return XFuture.completedFuture(null);
	}

	public boolean isClosed() {
		return isClosed;
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
	public ChannelSession getSession() {
		
		return null;
	}

	@Override
	public boolean isSslChannel() {
		
		return false;
	}

	@Override
	public void setReuseAddress(boolean b) {
		
		
	}

	@Override
	public String getChannelId() {
		
		return null;
	}

	@Override
	public XFuture<Void> bind(SocketAddress addr) {
		
		return null;
	}

	@Override
	public boolean isBlocking() {
		
		return false;
	}

	@Override
	public boolean isBound() {
		
		return false;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		
		return null;
	}

	@Override
	public boolean getKeepAlive() {
		
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		
		
	}

	public void setConnectFuture(XFuture<Void> future) {
		super.addValueToReturn(Method.CONNECT, future);
	}

	public void addWriteResponse(XFuture<Void> future) {
		super.addValueToReturn(Method.WRITE, future);
	}

	public HttpPayload getLastWriteParam() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.WRITE);
		if(params.size() != 1)
			throw new IllegalStateException("Write should have been called only once and was not.  times="+params.size());
		return (HttpPayload) params.get(0).getArgs()[0];
	}
	
	public DataListener getConnectedListener() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.CONNECT);
		if(params.size() != 1)
			throw new IllegalStateException("Connect should be called exactly once");
		return (DataListener) params.get(0).getArgs()[1];
	}

	public XFuture<Void> writeResponse(HttpResponse response) {
		ByteBuffer buffer = parser.marshalToByteBuffer(response);
		return listener.incomingData(this, buffer);
	}

	public void simulateClose() {
		listener.farEndClosed(this);
	}

	@Override
	public Boolean isServerSide() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
package org.webpieces.httpfrontend2.api.mock2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.MarshalState;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;
import org.webpieces.util.bytes.Hex;
import org.webpieces.util.exceptions.SneakyThrow;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MockHttp1Channel extends MockSuperclass implements TCPChannel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private enum Method implements MethodEnum {
		INCOMING_FRAME
	}
	
	private DataListener listener;
	private boolean isClosed;
	private ChannelSession session = new ChannelSessionImpl();
	private HttpParser parser;
	private Memento memento;
	private MarshalState marshalState;

	public MockHttp1Channel() {
		BufferPool pool = new TwoPools("pl", new SimpleMeterRegistry());
		parser = HttpParserFactory.createParser("a", new SimpleMeterRegistry(), pool);
		memento = parser.prepareToParse();
		marshalState = parser.prepareToMarshal();
	}
	
	@Override
	public XFuture<Void> connect(SocketAddress addr, DataListener listener) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	public void sendHexToSvr(String hex) {
		byte[] bytes = Hex.parseHexBinary(hex.replaceAll("\\s+",""));
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		XFuture<Void> fut = listener.incomingData(this, buf);
		try {
			fut.get(2, TimeUnit.SECONDS);
		} catch(ExecutionException | InterruptedException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public void sendToSvr(HttpPayload msg) {
		XFuture<Void> fut = sendToSvrAsync(msg);
		try {
			fut.get(2, TimeUnit.SECONDS);
		} catch(ExecutionException | InterruptedException | TimeoutException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	public XFuture<Void> sendToSvrAsync(HttpPayload msg) {
		if(listener == null)
			throw new IllegalStateException("Not connected so we cannot write back");
		ByteBuffer buf = parser.marshalToByteBuffer(marshalState, msg);
		XFuture<Void> fut = listener.incomingData(this, buf);
		return fut;
	}
	
	public XFuture<Void> sendToSvr(ByteBuffer buf) {
		return listener.incomingData(this, buf);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public XFuture<Void> write(ByteBuffer b) {
		DataWrapper data = dataGen.wrapByteBuffer(b);
		parser.parse(memento, data);
		List<HttpPayload> payloads = memento.getParsedMessages();
		return (XFuture<Void>) super.calledMethod(Method.INCOMING_FRAME, payloads);
	}

	public HttpPayload getFrameAndClear() {
		List<HttpPayload> msgs = getFramesAndClear();
		if(msgs.size() != 1)
			throw new IllegalStateException("not correct number of responses.  number="+msgs.size()+" but expected 1.  list="+msgs);
		return msgs.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<HttpPayload> getFramesAndClear() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_FRAME);
		Stream<HttpPayload> retVal = calledMethodList.map(p -> (List<HttpPayload>)p.getArgs()[0])
														.flatMap(Collection::stream);

		//clear out read values
		this.calledMethods.remove(Method.INCOMING_FRAME);
		
		return retVal.collect(Collectors.toList());
	}
	
	public void setIncomingFrameDefaultReturnValue(XFuture<Void> future) {
		super.setDefaultReturnValue(Method.INCOMING_FRAME, future);
	}
	
	
	
	@Override
	public XFuture<Void> close() {
		isClosed = true;
		return null;
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
		return session ;
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
		return XFuture.completedFuture(null);
	}

	@Override
	public boolean isBlocking() {
		
		return false;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
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
	
	public void assertNoIncomingMessages() {
		List<ParametersPassedIn> list = this.calledMethods.get(Method.INCOMING_FRAME);
		if(list == null)
			return;
		else if(list.size() != 0)
			throw new IllegalStateException("expected no method calls but method was called "+list.size()+" times.  list="+list);
	}

	public void setDataListener(DataListener dataListener) {
		this.listener = dataListener;
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

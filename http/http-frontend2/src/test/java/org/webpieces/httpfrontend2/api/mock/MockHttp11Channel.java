package org.webpieces.httpfrontend2.api.mock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.httpparser.api.dto.HttpMessage;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.util.ChannelSessionImpl;

public class MockHttp11Channel extends MockSuperclass implements TCPChannel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	private boolean isClosed;
	private ChannelSessionImpl session = new ChannelSessionImpl();
	private HttpParser parser;
	private DataListener dataListener;
	private Memento unmarshalState;

	private enum Method implements MethodEnum {
		INCOMING_FRAME
	}
	
	public MockHttp11Channel() {
		BufferPool bufferPool = new BufferCreationPool();
		parser = HttpParserFactory.createParser(bufferPool);
		unmarshalState = parser.prepareToParse();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		DataWrapper data = dataGen.wrapByteBuffer(b);
		parser.parse(unmarshalState, data);
		List<HttpPayload> parsedFrames = unmarshalState.getParsedMessages();
		return (CompletableFuture<Channel>) super.calledMethod(Method.INCOMING_FRAME, parsedFrames);
	}
	
	public void assertNoMessages() {
		List<HttpMessage> msgs = getHttpResponsesAndClear();
		if(msgs.size() > 0)
			throw new IllegalStateException("http messages recieved="+msgs);
	}
	
	public HttpMessage getHttpResponseAndClear() {
		List<HttpMessage> msgs = getHttpResponsesAndClear();
		if(msgs.size() != 1)
			throw new IllegalStateException("not correct number of responses.  number="+msgs.size()+" but expected 1.  list="+msgs);
		return msgs.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<HttpMessage> getHttpResponsesAndClear() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_FRAME);
		Stream<HttpMessage> retVal = calledMethodList.map(p -> (List<HttpMessage>)p.getArgs()[0])
														.flatMap(Collection::stream);

		//clear out read values
		this.calledMethods.remove(Method.INCOMING_FRAME);
		
		return retVal.collect(Collectors.toList());
	}
	
	@Override
	public void setReuseAddress(boolean b) {
		
	}

	@Override
	public void setName(String string) {
		
	}

	@Override
	public String getChannelId() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void bind(SocketAddress addr) {
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
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		return null;
	}

	@Override
	public CompletableFuture<Channel> close() {
		isClosed = true;
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> registerForReads() {
		return null;
	}

	@Override
	public CompletableFuture<Channel> unregisterForReads() {
		return null;
	}

	@Override
	public boolean isRegisteredForReads() {
		return false;
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
		return session;
	}

	@Override
	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
	}

	@Override
	public int getMaxBytesBackupSize() {
		return 0;
	}

	@Override
	public boolean getKeepAlive() {
		return false;
	}

	@Override
	public void setKeepAlive(boolean b) {
		
	}

	@Override
	public boolean isSslChannel() {
		return false;
	}


	public void setDataListener(DataListener dataListener) {
		this.dataListener = dataListener; 
	}

	public void writeHttpMsg(HttpMessage msg) {
		ByteBuffer buf = parser.marshalToByteBuffer(msg);
		dataListener.incomingData(this, buf);
	}

}

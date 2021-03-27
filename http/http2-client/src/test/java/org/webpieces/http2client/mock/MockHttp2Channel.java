package org.webpieces.http2client.mock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.exceptions.SneakyThrow;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MockHttp2Channel extends MockSuperclass implements TCPChannel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private enum Method implements MethodEnum {
		INCOMING_FRAME
	}
	
	private boolean prefaceReceived;
	private HpackParser parser;
	private Http2Parser frameParser;
	private UnmarshalState unmarshalState;
	private boolean connected;
	private MarshalState marshalState;
	private DataListener listener;
	private boolean isClosed;

	public MockHttp2Channel() {
		BufferPool bufferPool = new TwoPools("pl", new SimpleMeterRegistry());
		parser = HpackParserFactory.createParser(bufferPool, false);
		unmarshalState = parser.prepareToUnmarshal("mockChannel", 4096, 4096, 4096);
		BufferPool pool = new TwoPools("pl", new SimpleMeterRegistry());
		frameParser = Http2ParserFactory.createParser(pool);
	}
	
	@Override
	public CompletableFuture<Void> connect(SocketAddress addr, DataListener listener) {
		if(connected)
			throw new IllegalStateException("already connected");
		else if(listener == null)
			throw new IllegalArgumentException("listener can't be null");
		
		connected = true;
		this.marshalState = parser.prepareToMarshal(4096, 4096);
		this.listener = listener;
		return CompletableFuture.completedFuture(null);
	}

	public void writeHexBack(String hex) {
		byte[] bytes = DatatypeConverter.parseHexBinary(hex.replaceAll("\\s+",""));
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(this, buf);		
	}

	public void write(Http2Msg msg) {
		CompletableFuture<Void> fut = writeAsync(msg);
		try {
			fut.get(2, TimeUnit.SECONDS);
		} catch(Exception e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public CompletableFuture<Void> writeAsync(Http2Msg msg) {
		if(listener == null)
			throw new IllegalStateException("Not connected so we cannot write back");
		DataWrapper data = parser.marshal(marshalState, msg);
		byte[] bytes = data.createByteArray();
		if(bytes.length == 0)
			throw new IllegalArgumentException("how do you marshal to 0 bytes...WTF");
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		return listener.incomingData(this, buf);
	}
	
	public void writeFrame(Http2Frame frame) {
		DataWrapper data = frameParser.marshal(frame);
		byte[] bytes = data.createByteArray();
		if(bytes.length == 0)
			throw new IllegalArgumentException("how do you marshal to 0 bytes...WTF");
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(this, buf);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> write(ByteBuffer b) {
		if(!prefaceReceived) {
			//copy and store preface
			ByteBuffer prefaceBuffer = ByteBuffer.allocate(b.remaining());
			prefaceBuffer.put(b);
			prefaceReceived = true;
			Preface preface = new Preface(prefaceBuffer);
			List<Http2Msg> msgs = new ArrayList<>();
			msgs.add(preface);
			return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_FRAME, msgs);
		}
		
		return processData(b);
	}

	@SuppressWarnings("unchecked")
	private CompletableFuture<Void> processData(ByteBuffer b) {
		DataWrapper data = dataGen.wrapByteBuffer(b);
		parser.unmarshal(unmarshalState, data);
		List<Http2Msg> parsedFrames = unmarshalState.getParsedFrames();
		return (CompletableFuture<Void>) super.calledMethod(Method.INCOMING_FRAME, parsedFrames);
	}

	public Http2Msg getFrameAndClear() {
		List<Http2Msg> msgs = getFramesAndClear();
		if(msgs.size() != 1)
			throw new IllegalStateException("not correct number of responses.  number="+msgs.size()+" but expected 1.  list="+msgs);
		return msgs.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<Http2Msg> getFramesAndClear() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_FRAME);
		Stream<Http2Msg> retVal = calledMethodList.map(p -> (List<Http2Msg>)p.getArgs()[0])
														.flatMap(Collection::stream);

		//clear out read values
		this.calledMethods.remove(Method.INCOMING_FRAME);
		
		return retVal.collect(Collectors.toList());
	}
	
	public void setIncomingFrameDefaultReturnValue(CompletableFuture<Void> future) {
		super.setDefaultReturnValue(Method.INCOMING_FRAME, future);
	}
	
	public void addWriteResponse(CompletableFuture<Object> future) {
		super.addValueToReturn(Method.INCOMING_FRAME, future);
	}
	
	@Override
	public CompletableFuture<Void> close() {
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
	public CompletableFuture<Void> bind(SocketAddress addr) {
		return CompletableFuture.completedFuture(null);
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

	@Override
	public String toString() {
		return "MockHttp2Channel1";
	}

	public DataListener getConnectedListener() {
		return listener;
	}

	@Override
	public Boolean isServerSide() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

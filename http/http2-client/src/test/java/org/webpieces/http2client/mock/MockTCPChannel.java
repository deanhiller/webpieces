package org.webpieces.http2client.mock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class MockTCPChannel extends MockSuperclass implements TCPChannel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private enum Method implements MethodEnum {
		INCOMING_FRAME
	}
	
	private ByteBuffer prefaceBuffer;
	private boolean prefaceReceived;
	private HpackParser parser;
	private UnmarshalState unmarshalState;
	private boolean connected;
	private SocketWriter writer;

	public MockTCPChannel() {
		BufferPool bufferPool = new BufferCreationPool();
		parser = HpackParserFactory.createParser(bufferPool, false);
		unmarshalState = parser.prepareToUnmarshal(4096, 4096, 4096);
	}
	
	public void clear() {
		super.clear();
	}
	
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		if(connected)
			throw new IllegalStateException("already connected");
		else if(listener == null)
			throw new IllegalArgumentException("listener can't be null");
		
		connected = true;
		MarshalState marshalState = parser.prepareToMarshal(4096, 4096);
		writer = new SocketWriter(this, parser, marshalState, listener);
		return CompletableFuture.completedFuture(this);
	}

	@Override
	public CompletableFuture<Channel> write(ByteBuffer b) {
		if(!prefaceReceived) {
			//copy and store preface
			prefaceBuffer = ByteBuffer.allocate(b.remaining());
			prefaceBuffer.put(b);
			prefaceReceived = true;
			return CompletableFuture.completedFuture(null);
		}
		
		return processData(b);
	}

	private CompletableFuture<Channel> processData(ByteBuffer b) {
		DataWrapper data = dataGen.wrapByteBuffer(b);
		parser.unmarshal(unmarshalState, data);
		List<Http2Msg> parsedFrames = unmarshalState.getParsedFrames();
		for(Http2Msg msg : parsedFrames) {
			super.calledMethod(Method.INCOMING_FRAME, msg);
		}
		
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Channel> close() {
		return null;
	}

	@Override
	public CompletableFuture<Channel> registerForReads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Channel> unregisterForReads() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRegisteredForReads() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ChannelSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxBytesWriteBackupSize(int maxBytesBackup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxBytesBackupSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSslChannel() {
		// TODO Auto-generated method stub
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
	public String getChannelId() {
		// TODO Auto-generated method stub
		return null;
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

	public Http2Msg getFrameAndClear() {
		Stream<ParametersPassedIn> calledMethodList = super.getCalledMethods(Method.INCOMING_FRAME);
		Stream<Http2Msg> retVal = calledMethodList.map(p -> (Http2Msg)p.getArgs()[0]);
		Http2Msg[] array = retVal.toArray(Http2Msg[]::new);
		if(array.length != 1)
			throw new IllegalStateException("not correct number of responses.  number="+array.length+" but expected 1");
		
		List<ParametersPassedIn> params = this.calledMethods.get(Method.INCOMING_FRAME);
		params.clear();
		
		return array[0];
	}

	public SocketWriter getSocketWriter() {
		return writer;
	}

}

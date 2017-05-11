package org.webpieces.httpfrontend2.api.mock2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

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
import org.webpieces.nio.impl.util.ChannelSessionImpl;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.MarshalState;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.SettingsFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class MockHttp2Channel extends MockSuperclass implements TCPChannel {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private enum Method implements MethodEnum {
		INCOMING_FRAME
	}
	
	private HpackParser parser;
	private Http2Parser frameParser;
	private UnmarshalState unmarshalState;
	private MarshalState marshalState;
	private DataListener listener;
	private boolean isClosed;
	private ChannelSession session = new ChannelSessionImpl();

	public MockHttp2Channel() {
		BufferPool bufferPool = new BufferCreationPool();
		parser = HpackParserFactory.createParser(bufferPool, false);
		unmarshalState = parser.prepareToUnmarshal(4096, 4096, 4096);
		marshalState = parser.prepareToMarshal(4096, 4096);
		BufferPool pool = new BufferCreationPool();
		frameParser = Http2ParserFactory.createParser(pool);
	}
	
	@Override
	public CompletableFuture<Channel> connect(SocketAddress addr, DataListener listener) {
		throw new UnsupportedOperationException("not implemented but could easily be with a one liner");
	}

	public void sendPrefaceAndSettings(SettingsFrame settings) {
		String preface = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";
		byte[] bytes = preface.getBytes(StandardCharsets.UTF_8);
		DataWrapper data = parser.marshal(marshalState, settings);
		DataWrapper prefaceWrapper = dataGen.wrapByteArray(bytes);
		DataWrapper all = dataGen.chainDataWrappers(prefaceWrapper, data);
		ByteBuffer buf = ByteBuffer.wrap(all.createByteArray());
		listener.incomingData(this, buf);
	}
	
	public void writeHexBack(String hex) {
		byte[] bytes = DatatypeConverter.parseHexBinary(hex.replaceAll("\\s+",""));
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(this, buf);		
	}
	
	public void write(Http2Msg msg) {
		if(listener == null)
			throw new IllegalStateException("Not connected so we cannot write back");
		DataWrapper data = parser.marshal(marshalState, msg);
		byte[] bytes = data.createByteArray();
		if(bytes.length == 0)
			throw new IllegalArgumentException("how do you marshal to 0 bytes...WTF");
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		listener.incomingData(this, buf);
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
	public CompletableFuture<Channel> write(ByteBuffer b) {		
		DataWrapper data = dataGen.wrapByteBuffer(b);
		parser.unmarshal(unmarshalState, data);
		List<Http2Msg> parsedFrames = unmarshalState.getParsedFrames();
		return (CompletableFuture<Channel>) super.calledMethod(Method.INCOMING_FRAME, parsedFrames);
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
	
	public void setIncomingFrameDefaultReturnValue(CompletableFuture<Channel> future) {
		super.setDefaultReturnValue(Method.INCOMING_FRAME, future);
	}
	
	
	
	@Override
	public CompletableFuture<Channel> close() {
		isClosed = true;
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
		return session ;
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
		return isClosed;
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

}

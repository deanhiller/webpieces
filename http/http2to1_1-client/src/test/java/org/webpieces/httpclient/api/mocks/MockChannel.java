package org.webpieces.httpclient.api.mocks;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2translations.api.Http1_1ToHttp2;
import org.webpieces.http2translations.api.Http2ToHttp1_1;
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

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class MockChannel extends MockSuperclass implements TCPChannel {
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private HttpStatefulParser parser;
	private DataListener listener;

	private enum Method implements MethodEnum {
		CONNECT, WRITE
	}

	public MockChannel() {
		parser = HttpParserFactory.createStatefulParser(new BufferCreationPool());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> connect(SocketAddress addr, DataListener listener) {
		if(this.listener != null)
			throw new IllegalStateException("connect should only be called once");
		this.listener = listener;
		return (CompletableFuture<Void>) super.calledMethod(Method.CONNECT, addr, listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<Void> write(ByteBuffer b) {
		DataWrapper wrapper = dataGen.wrapByteBuffer(b);
		List<HttpPayload> parsedData = parser.parse(wrapper);
		if(parsedData.size() != 1)
			throw new IllegalArgumentException("The impl should be writing out full single payloads each write call");
		HttpPayload payload = parsedData.get(0);
		Http2Msg http2 = Http1_1ToHttp2.translate(payload, false);
		return (CompletableFuture<Void>) super.calledMethod(Method.WRITE, http2);
	}

	@Override
	public CompletableFuture<Void> close() {
		// TODO Auto-generated method stub
		return null;
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
	public CompletableFuture<Void> bind(SocketAddress addr) {
		// TODO Auto-generated method stub
		return null;
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

	public void setConnectFuture(CompletableFuture<Void> future) {
		super.addValueToReturn(Method.CONNECT, future);
	}

	public void addWriteResponse(CompletableFuture<Void> future) {
		super.addValueToReturn(Method.WRITE, future);
	}

	public Http2Msg getLastWriteParam() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.WRITE);
		if(params.size() != 1)
			throw new IllegalStateException("Write should have been called only once and was not.  times="+params.size());
		return (Http2Msg) params.get(0).getArgs()[0];
	}
	
	public DataListener getConnectedListener() {
		List<ParametersPassedIn> params = super.getCalledMethodList(Method.CONNECT);
		if(params.size() != 1)
			throw new IllegalStateException("Connect should be called exactly once");
		return (DataListener) params.get(0).getArgs()[1];
	}

	public CompletableFuture<Void> writeResponse(Http2Response response1) {
		HttpResponse response = Http2ToHttp1_1.translateResponse(response1);
		ByteBuffer buffer = parser.marshalToByteBuffer(response);
		return listener.incomingData(this, buffer);
	}

	
}

package org.webpieces.frontend2.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpRequestListener;
import org.webpieces.frontend2.api.SocketInfo;

import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

public class Layer2Http2Handler {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Http2ServerEngineFactory svrEngineFactory;
	private HttpRequestListener httpListener;
	private SocketInfo socketInfo;

	public Layer2Http2Handler(
			Http2ServerEngineFactory svrEngineFactory, 
			HttpRequestListener httpListener, boolean isHttps
	) {
		this.svrEngineFactory = svrEngineFactory;
		this.httpListener = httpListener;
		this.socketInfo = new SocketInfo(ProtocolType.HTTP2, isHttps);
	}

	public void initialize(FrontendSocketImpl socket) {
		Layer3Http2EngineListener listener = new Layer3Http2EngineListener(socket, httpListener, socketInfo);
		Http2ServerEngine engine = svrEngineFactory.createEngine(listener);
		socket.setHttp2Engine(engine);
		
		engine.intialize();
	}
	
	public void incomingData(FrontendSocketImpl socket, ByteBuffer b) {
		DataWrapper wrapper = dataGen.wrapByteBuffer(b);
		incomingData(socket, wrapper);
	}
	
	public void incomingData(FrontendSocketImpl socket, DataWrapper data) {
		Http2ServerEngine engine = socket.getHttp2Engine();
		engine.parse(data);
	}

	public void farEndClosed(FrontendSocketImpl socket) {
		Http2ServerEngine engine = socket.getHttp2Engine();
		engine.farEndClosed();
	}

	public void setBoundAddr(InetSocketAddress localAddr) {
		socketInfo.setBoundAddress(localAddr);
	}

}

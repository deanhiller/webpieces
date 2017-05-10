package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpRequestListener;

import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

public class Layer2Http2Handler {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Http2ServerEngineFactory svrEngineFactory;
	private HttpRequestListener httpListener;

	public Layer2Http2Handler(
			Http2ServerEngineFactory svrEngineFactory, 
			HttpRequestListener httpListener
	) {
		this.svrEngineFactory = svrEngineFactory;
		this.httpListener = httpListener;
	}

	public void initialize(FrontendSocketImpl socket) {
		Layer3Http2EngineListener listener = new Layer3Http2EngineListener(socket, httpListener);
		Http2ServerEngine engine = svrEngineFactory.createEngine(listener);
		socket.setHttp2Engine(engine);
		
		engine.intialize();
	}
	
	public void incomingData(FrontendSocketImpl socket, ByteBuffer b) {
		Http2ServerEngine engine = socket.getHttp2Engine();
		DataWrapper data = dataGen.wrapByteBuffer(b);
		engine.parse(data);
	}

	public void farEndClosed(FrontendSocketImpl socket) {
		Http2ServerEngine engine = socket.getHttp2Engine();
		engine.farEndClosed();
	}

}

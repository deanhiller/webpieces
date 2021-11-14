package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.StreamListener;

import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;

public class Layer2Http2Handler {

	private static final Logger log = LoggerFactory.getLogger(Layer2Http2Handler.class);
	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Http2ServerEngineFactory svrEngineFactory;
	private StreamListener httpListener;

	public Layer2Http2Handler(
			Http2ServerEngineFactory svrEngineFactory, 
			StreamListener httpListener
	) {
		this.svrEngineFactory = svrEngineFactory;
		this.httpListener = httpListener;
	}

	public XFuture<Void> initialize(FrontendSocketImpl socket) {
		Layer3Http2EngineListener listener = new Layer3Http2EngineListener(socket, httpListener);
		Http2ServerEngine engine = svrEngineFactory.createEngine(socket+"", listener);
		socket.setHttp2Engine(engine);
		
		return engine.intialize();
	}
	
	public XFuture<Void> incomingData(FrontendSocketImpl socket, ByteBuffer b) {
		DataWrapper wrapper = dataGen.wrapByteBuffer(b);
		return incomingData(socket, wrapper);
	}
	
	public XFuture<Void> incomingData(FrontendSocketImpl socket, DataWrapper data) {
		Http2ServerEngine engine = socket.getHttp2Engine();
		return engine.parse(data);
	}

	public void farEndClosed(FrontendSocketImpl socket) {
		log.error("far end closed="+socket);
		Http2ServerEngine engine = socket.getHttp2Engine();
		engine.farEndClosed();
	}

}

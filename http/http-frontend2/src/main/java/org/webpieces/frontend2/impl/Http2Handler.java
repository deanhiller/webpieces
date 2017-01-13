package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.frontend2.api.HttpRequestListener;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.http2engine.api.server.Http2ServerEngine;
import com.webpieces.http2engine.api.server.Http2ServerEngineFactory;
import com.webpieces.http2engine.impl.shared.HeaderSettings;

public class Http2Handler {

	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private Http2ServerEngineFactory svrEngineFactory;
	private HpackParser http2Parser;
	private HttpRequestListener httpListener;
	private HeaderSettings localSettings;
	private Executor backupPool;

	public Http2Handler(
			Http2ServerEngineFactory svrEngineFactory, 
			HpackParser http2Parser,
			HttpRequestListener httpListener,
			HeaderSettings localSettings,
			Executor backupPool
	) {
				this.svrEngineFactory = svrEngineFactory;
				this.http2Parser = http2Parser;
				this.httpListener = httpListener;
				this.localSettings = localSettings;
				this.backupPool = backupPool;
	}

	public void initialize(FrontendSocketImpl socket) {
		EngineListener listener = new EngineListener(socket, httpListener);
		Http2ServerEngine engine = svrEngineFactory.createEngine(http2Parser, backupPool, listener, localSettings);
		socket.setHttp2Engine(engine);
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

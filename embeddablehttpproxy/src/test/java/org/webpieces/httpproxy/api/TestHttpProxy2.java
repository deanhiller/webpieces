package org.webpieces.httpproxy.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.frontend.api.HttpFrontendFactory;
import org.webpieces.frontend.api.HttpFrontendManager;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.handlers.AsyncDataListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.DirectExecutor;

import com.google.inject.Binder;
import com.google.inject.Module;

public class TestHttpProxy2 {
	private BufferPool bufferPool = new BufferCreationPool();
	private HttpParser parser = HttpParserFactory.createParser(bufferPool);
	private MockTimer timer = new MockTimer();
	private MockAsyncServerManager mockAsyncServer = new MockAsyncServerManager();
	private MockTcpChannel mockTcpChannel = new MockTcpChannel();
	private DataListener dataListener;
	
	@Before
	public void setUp() throws IOException {
		ProxyConfig config = new ProxyConfig();
		HttpProxy proxy = HttpProxyFactory.createHttpProxy("myproxy", new TestModule(), config);
		proxy.start();

		List<AsyncDataListener> serverListeners = mockAsyncServer.getServerListeners();
		dataListener = serverListeners.get(0);
	}
	
	private byte[] unwrap(ByteBuffer buffer) {
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		return data;
	}
	
	@Test
	public void testBasicProxy() throws IOException, ClassNotFoundException {
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri("http://www.deano.com"));
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		byte[] array = unwrap(parser.marshalToByteBuffer(req));
		
		ByteBuffer buffer = ByteBuffer.wrap(array);
		
		dataListener.incomingData(mockTcpChannel, ByteBuffer.allocate(0));
		dataListener.incomingData(mockTcpChannel, buffer);
	}
	
	private class TestModule implements Module {
		@Override
		public void configure(Binder binder) {
			HttpFrontendManager frontEnd = HttpFrontendFactory.createFrontEnd(mockAsyncServer, timer, bufferPool);
			binder.bind(HttpFrontendManager.class).toInstance(frontEnd);
			binder.bind(Executor.class).toInstance(new DirectExecutor());
		}
	}
}

package org.webpieces.throughput.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.exceptions.SneakyThrow;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class ServerHttp11Sync {
	private static final Logger log = LoggerFactory.getLogger(ServerHttp11Sync.class);

	public XFuture<InetSocketAddress> start() {
		try {
			return startImpl();
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	public XFuture<InetSocketAddress> startImpl() throws IOException {
    	log.error("running SYNC HTTP1.1 SERVER");

		ServerSocket server = new ServerSocket(0);
		
    	Runnable r = new ServerRunnable(server);

    	Thread t = new Thread(r);
    	t.setName("echoServer");
    	t.start();
    	
    	InetSocketAddress address = (InetSocketAddress) server.getLocalSocketAddress();
    	return XFuture.completedFuture(address);
	}

    private static class ServerRunnable implements Runnable {
    	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

		private HttpStatefulParser parser = HttpParserFactory.createStatefulParser("a", new SimpleMeterRegistry(), new TwoPools("pl", new SimpleMeterRegistry()));
		private ServerSocket server;

		public ServerRunnable(ServerSocket server) {
			this.server = server;
		}

		public void runImpl() throws IOException {
	        Socket socket = server.accept();
	        InputStream input = socket.getInputStream();
	        OutputStream output = socket.getOutputStream();

	        while(true) {
		        byte[] bytes = new byte[1024];
	        	int numRead = input.read(bytes);
	        	DataWrapper data = dataGen.wrapByteArray(bytes, 0, numRead);
	        	List<HttpPayload> payloads = parser.parse(data);
	        	
	        	for(HttpPayload p : payloads) {
	        		p.getMessageType();
	        		HttpResponse resp = RequestCreator.createHttp1_1Response();
	        		ByteBuffer buffer = parser.marshalToByteBuffer(resp);
	        		byte[] b = new byte[buffer.remaining()];
	        		buffer.get(b);
	        		output.write(b);
	        	}
	        }
		}

		@Override
		public void run() {
			try {
				runImpl();
			} catch (IOException e) {
				log.error("Exception", e);
			}
		}
    }
}

package org.webpieces.throughput.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.HttpStatefulParser;
import org.webpieces.httpparser.api.dto.HttpPayload;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class ServerHttp1_1Sync {
	private static final Logger log = LoggerFactory.getLogger(ServerHttp1_1Sync.class);

	public CompletableFuture<InetSocketAddress> start() {
		try {
			return startImpl();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public CompletableFuture<InetSocketAddress> startImpl() throws IOException {
    	log.error("running SYNC HTTP1.1 SERVER");

		ServerSocket server = new ServerSocket(0);
		
    	Runnable r = new ServerRunnable(server);

    	Thread t = new Thread(r);
    	t.setName("echoServer");
    	t.start();
    	
    	InetSocketAddress address = (InetSocketAddress) server.getLocalSocketAddress();
    	return CompletableFuture.completedFuture(address);
	}

    private static class ServerRunnable implements Runnable {
    	private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

		private HttpStatefulParser parser = HttpParserFactory.createStatefulParser(new BufferCreationPool());
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

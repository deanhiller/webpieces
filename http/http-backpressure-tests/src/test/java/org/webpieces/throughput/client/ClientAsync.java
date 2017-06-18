package org.webpieces.throughput.client;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.webpieces.frontend2.api.Protocol;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

public class ClientAsync {
	private static final Logger log = LoggerFactory.getLogger(ClientAsync.class);

	private Http2Client client;
	private Protocol protocol;

	public ClientAsync(Http2Client client, Protocol protocol) {
		this.client = client;
		this.protocol = protocol;
	}

	public void runAsyncClient(InetSocketAddress svrAddress) {
		Http2Socket socket = client.createHttpSocket("clientSocket");
		CompletableFuture<Void> connect = socket.connect(svrAddress);
		
		CompletableFuture<Void> future = connect.thenApply(v -> startWriteThread(socket));

		try {
			future.get(2, TimeUnit.SECONDS);
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	private Void startWriteThread(Http2Socket socket) {
		Thread t = new Thread(new Writer(socket));
		t.setName("clientWriterThread");
		t.start();
		
		return null;
	}
	
	private class Writer implements Runnable {

		private Http2Socket socket;

		public Writer(Http2Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				runImpl();
			} catch (Throwable e) {
				log.error("Exception", e);
			}
		}
		
		public void runImpl() throws InterruptedException, ExecutionException, TimeoutException {
	    	log.error("ASYNC "+protocol+" CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
	    	log.info("info messages automatically show up in black");
	    	
			ResponseHandler responseListener = new ResponseCounterListener();
			
			while(true) {
				Http2Request request = RequestCreator.createHttp2Request();
				
				StreamHandle stream = socket.openStream();
				CompletableFuture<StreamWriter> future = stream.process(request, responseListener);
				
				//the future puts the perfect amount of backpressure or performance will tank
				//(ie. comment out this line and watch performance tank)
				future.get(10, TimeUnit.SECONDS);
			}
		}
		
	}
}

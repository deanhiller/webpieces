package org.webpieces.throughput;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;

public class ClientAsync {
	private static final Logger log = LoggerFactory.getLogger(ClientAsync.class);

	private Http2Client client;

	public ClientAsync(Http2Client client) {
		this.client = client;
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
	    	log.error("ASYNC CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
	    	log.info("info messages automatically show up in black");
	    	
			ResponseHandler responseListener = new ResponseCounterListener();
			
			while(true) {
				Http2Request request = RequestCreator.createHttp2Request();
				
				StreamHandle stream = socket.openStream();
				stream.process(request, responseListener);
			}
		}
		
	}
}

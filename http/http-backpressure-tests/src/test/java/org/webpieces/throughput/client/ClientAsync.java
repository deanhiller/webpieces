package org.webpieces.throughput.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.webpieces.frontend2.api.Protocol;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.throughput.AsyncConfig;
import org.webpieces.throughput.RequestCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.http2engine.api.ResponseHandler;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2engine.api.StreamWriter;

public class ClientAsync {
	private static final Logger log = LoggerFactory.getLogger(ClientAsync.class);

	private Http2Client client;
	private Protocol protocol;
	private AsyncConfig config;

	public ClientAsync(Http2Client client, AsyncConfig config, Protocol protocol) {
		this.client = client;
		this.config = config;
		this.protocol = protocol;
	}

	public void runAsyncClient(InetSocketAddress svrAddress) {
		ResponseHandler responseListener = new ResponseCounterListener();
    	log.error("ASYNC "+protocol+" CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
    	log.info("info messages automatically show up in black");
    	
		List<Http2Socket> sockets = new ArrayList<>();
		for(int i = 0; i < config.getNumSockets(); i++) {	
			try {
				log.info("connecting socket num="+i);
				Http2Socket socket = client.createHttpSocket();
				CompletableFuture<Void> connect = socket.connect(svrAddress);
				connect.get(2, TimeUnit.SECONDS);
				sockets.add(socket);
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
		}
		
		for(int i = 0; i < config.getNumSockets(); i++) {
			Http2Socket socket = sockets.get(i);
			startWriteThread(socket, i, responseListener);
		}
	}
	
	private Void startWriteThread(Http2Socket socket, int i, ResponseHandler handler) {

		Thread t = new Thread(new Writer(socket, handler));
		t.setName("clientWriter"+i);
		t.start();
		
		return null;
	}
	
	private class Writer implements Runnable {

		private Http2Socket socket;
		private ResponseHandler handler;

		public Writer(Http2Socket socket, ResponseHandler handler) {
			this.socket = socket;
			this.handler = handler;
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
			while(true) {
				Http2Request request = RequestCreator.createHttp2Request();
				
				StreamHandle stream = socket.openStream();
				CompletableFuture<StreamWriter> future = stream.process(request, handler);
				
				//the future puts the perfect amount of backpressure or performance will tank
				//(ie. comment out this line and watch performance tank)
				future.get(100, TimeUnit.SECONDS);
			}
		}
		
	}
}

package org.webpieces.throughput.client;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.util.HostWithPort;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend2.api.Protocol;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.throughput.AsyncConfig;
import org.webpieces.throughput.RequestCreator;
import org.webpieces.util.SneakyThrow;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.streaming.RequestStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import com.webpieces.http2.api.streaming.StreamWriter;

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

	public void runAsyncClient(HostWithPort svrAddress) {
		ResponseStreamHandle responseListener = new ResponseCounterListener();
    	log.error("ASYNC "+protocol+" CLIENT: logging will log every 10 seconds as ERROR so it shows up in red");
    	log.info("info messages automatically show up in black");
    	
		List<Http2Socket> sockets = new ArrayList<>();
		for(int i = 0; i < config.getNumSockets(); i++) {	
			try {
				log.info("connecting socket num="+i);
				Http2Socket socket = client.createHttpSocket(new CloseListener());
				XFuture<Void> connect = socket.connect(svrAddress);
				connect.get(2, TimeUnit.SECONDS);
				sockets.add(socket);
			} catch(Throwable e) {
				throw SneakyThrow.sneak(e);
			}
		}
		
		for(int i = 0; i < config.getNumSockets(); i++) {
			Http2Socket socket = sockets.get(i);
			startWriteThread(socket, i, responseListener);
		}
	}
	
	private Void startWriteThread(Http2Socket socket, int i, ResponseStreamHandle handler) {

		Thread t = new Thread(new Writer(socket, handler));
		t.setName("clientWriter"+i);
		t.start();
		
		return null;
	}
	
	private class Writer implements Runnable {

		private Http2Socket socket;
		private ResponseStreamHandle handler;

		public Writer(Http2Socket socket, ResponseStreamHandle handler) {
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
				
				RequestStreamHandle stream = socket.openStream();
				StreamRef process = stream.process(request, handler);
				XFuture<StreamWriter> future = process.getWriter();
				
				//the future puts the perfect amount of backpressure or performance will tank
				//(ie. comment out this line and watch performance tank)
				future.get(100, TimeUnit.SECONDS);
			}
		}
		
	}
}

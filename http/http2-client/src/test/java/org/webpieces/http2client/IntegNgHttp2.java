package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.Http2ResponseListener;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class IntegNgHttp2 {

    private static final Logger log = LoggerFactory.getLogger(IntegNgHttp2.class);
    
    private static Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("deanClientThread"));
    private static SortedSet<Integer> sent = new TreeSet<>();
    private static SortedSet<Integer> completed = new TreeSet<>();
    private static SortedSet<Integer> completedPush = new TreeSet<>();

    private static class WorkItem implements Runnable {
    	private ChunkedResponseListener listener;
		private Http2Socket socket;
		private List<Http2Header> req;
		private int id;
		private int originalId;

		public WorkItem(Http2Socket socket, List<Http2Header> req2, int id, int originalId) {
    		this.socket = socket;
			this.req = req2;
			this.id = id;
			this.originalId = originalId;
			listener = new ChunkedResponseListener(id);
    	}
    	
    	public void run() {
    		try {
    			runImpl();
    		} catch(Throwable e) {
    			log.warn("Exception", e);
    		}
    	}
    	public void runImpl() {
            synchronized(sent) {
            	sent.add(id);
            }
    		
        	Http2Headers request = new Http2Headers(req);
            request.setEndOfStream(true);
            
    		socket.sendRequest(request, listener)
    				.exceptionally(e -> {
    					reportException(socket, e);
    					return null;
    				});

    		log.info("sent request.  ID="+id+" streamId="+request.getStreamId());

    		int numRun = id - originalId;
    		if(numRun >= 9) {
    			log.info("exiting running more.  numRun="+numRun+" id="+id+" orig="+originalId);
    			return;
    		}
    		
    		int newId = id+1;
    		WorkItem work = new WorkItem(socket, req, newId, originalId);
    		executor.execute(work);
    	}
    };
    
    public static void main(String[] args) throws InterruptedException {
        boolean isHttp = true;

        String path = "/";
        String host = "localhost";
        int port = 443;
        if(isHttp)
            port = 8080;

        if(host.equals("localhost"))
        	path = "/test/data.txt"; //IF jetty, use a path with a bigger download
        
        List<Http2Header> req = createRequest(host, isHttp, path);

        log.info("starting socket");

        InetSocketAddress addr = new InetSocketAddress(host, port);
        Http2Socket socket = IntegGoogleHttps.createHttpClient("clientSocket", isHttp, addr);
        
        socket
                .connect(addr, new ServerListenerImpl())
                .thenApply(s -> {
                    for(int i = 0; i < 1000; i+=100) {
                    	executor.execute(new WorkItem(socket, req, i, i));
                    }
                	return s;
                })
                .exceptionally(e -> {
                    reportException(socket, e);
                    return null;
                });

        Thread.sleep(100000000);
    }

    private static Void reportException(Http2Socket socket, Throwable e) {
        log.error("exception on socket="+socket, e);
        return null;
    }

	private static class ServerListenerImpl implements Http2ServerListener {

		@Override
		public void farEndClosed(Http2Socket socket) {
			log.info("far end closed");
		}

		@Override
		public void failure(Exception e) {
			log.warn("exception", e);
		}

		@Override
		public void incomingControlFrame(Http2Frame lowLevelFrame) {
			if(lowLevelFrame instanceof GoAwayFrame) {
				GoAwayFrame goAway = (GoAwayFrame) lowLevelFrame;
				DataWrapper debugData = goAway.getDebugData();
				String debug = debugData.createStringFrom(0, debugData.getReadableSize(), StandardCharsets.UTF_8);
				log.info("go away received.  debug="+debug);
			} else 
				throw new UnsupportedOperationException("not done yet.  frame="+lowLevelFrame);
		}
	}
	
	private static class ChunkedResponseListener implements Http2ResponseListener, PushPromiseListener {

		private int id;

		public ChunkedResponseListener(int id) {
			this.id = id;
		}

		@Override
		public CompletableFuture<Void> incomingPartialResponse(PartialStream response) {
			log.info("incoming part of response="+response);
			
			if(response.isEndOfStream()) {
				synchronized (completed) {
					completed.add(id);
				}
				log.info("completed="+completed.size()+" completedPus="+completedPush.size()+" sent="+sent.size()+" list="+completed);
			}
			
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public PushPromiseListener newIncomingPush(int streamId) {
			return this;
		}
		
		@Override
		public void serverCancelledRequest() {
			log.info("server cancelled request");
		}

		@Override
		public CompletableFuture<Void> incomingPushPromise(PartialStream response) {
			log.info("incoming push promise="+response);
			if(response.isEndOfStream()) {
				synchronized(this) {
					completedPush.add(id);
					log.info("completedPush="+completedPush+" sent="+sent.size());
				}
			}
			return CompletableFuture.completedFuture(null);
		}
	}

    private static List<Http2Header> createRequest(String host, boolean isHttp, String path) {
    	String scheme;
    	if(isHttp)
    		scheme = "http";
    	else
    		scheme = "https";
    	
    	List<Http2Header> headers = new ArrayList<>();
    	
        headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
        headers.add(new Http2Header(Http2HeaderName.AUTHORITY, host));
        headers.add(new Http2Header(Http2HeaderName.PATH, path));
        headers.add(new Http2Header(Http2HeaderName.SCHEME, scheme));
        headers.add(new Http2Header("host", host));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));

        return headers;
    }
}

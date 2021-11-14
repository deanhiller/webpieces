package org.webpieces.http2client.integ;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.util.threading.NamedThreadFactory;

public class IntegMultiThreaded {

    private static final Logger log = LoggerFactory.getLogger(IntegMultiThreaded.class);
    
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
			listener = new ChunkedResponseListener();
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
    		
            Http2Request request = new Http2Request(req);
            request.setEndOfStream(true);

			StreamRef streamRef = socket.openStream().process(request, listener);
			streamRef.getWriter().exceptionally(e -> {
    					reportException(socket, e);
    					return null;
    				});

    		log.info("sent request.  ID="+id+" streamId="+request.getStreamId());

    		int numRun = id - originalId;
    		if(numRun >= -1) {
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
		//String host = www.google.com; 
		//String host = "localhost"; //jetty
        String host = "nghttp2.org";
        int port = 443;
        if(isHttp)
            port = 80;

        if(host.equals("localhost")) {
        	path = "/test/data.txt"; //IF jetty, use a path with a bigger download
			port = 8443;
			if(isHttp)
				port = 8080;
		}
        
        List<Http2Header> req = createRequest(host, isHttp, path);

        log.info("starting socket");

        InetSocketAddress addr = new InetSocketAddress(host, port);
        Http2Socket socket = IntegSingleRequest.createHttpClient("clientSocket", isHttp, addr);
        
        socket
                .connect(addr)
                .thenApply(s -> {
                    for(int i = 0; i < 99; i+=100) {
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
	
	private static class ChunkedResponseListener implements ResponseStreamHandle, PushPromiseListener, PushStreamHandle {

		@Override
		public XFuture<StreamWriter> process(Http2Response response) {
			log.info("incoming part of response="+response);
			
			if(response.isEndOfStream()) {
				synchronized (completed) {
					completed.add(response.getStreamId());
				}
				log.info("completed="+completed.size()+" completedPus="+completedPush.size()+" sent="+sent.size()+" list="+completed);
			}
			
			return XFuture.completedFuture(null);
		}

		@Override
		public PushStreamHandle openPushStream() {
			return this;
		}
		
		@Override
		public XFuture<StreamWriter> processPushResponse(Http2Response response) {
			log.info("incoming push promise="+response);
			if(response.isEndOfStream()) {
				synchronized(this) {
					completedPush.add(response.getStreamId());
					log.info("completedPush="+completedPush+" sent="+sent.size());
				}
			}
			return XFuture.completedFuture(null);
		}

		@Override
		public XFuture<Void> cancel(CancelReason frame) {
			return XFuture.completedFuture(null);
		}
		@Override
		public XFuture<Void> cancelPush(CancelReason payload) {
			return XFuture.completedFuture(null);
		}
		@Override
		public XFuture<PushPromiseListener> process(Http2Push headers) {
			return XFuture.completedFuture(this);
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

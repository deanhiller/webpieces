package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class IntegMultithreadedNgHttp2 {

    private static final Logger log = LoggerFactory.getLogger(IntegMultithreadedNgHttp2.class);

    private static Executor executor = Executors.newFixedThreadPool(10, new NamedThreadFactory("deanClientThread"));
    private static SortedSet<Integer> sent = new TreeSet<>();
    private static SortedSet<Integer> completed = new TreeSet<>();
    private static SortedSet<Integer> completedPush = new TreeSet<>();
    
    private static class WorkItem implements Runnable {
    	private ChunkedResponseListener listener;
		private RequestSender socket;
		private HttpRequest req;
		private int id;
		private int originalId;

		public WorkItem(RequestSender socket, HttpRequest req, int id, int originalId) {
    		this.socket = socket;
			this.req = req;
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
    		
            socket.sendRequest(req, true, listener)
    				.exceptionally(e -> {
    					reportException(e);
    					return null;
    				});

    		log.info("sent request.  ID="+id+" streamId=");

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

        String host = "nghttp2.org";
        int port = 443;
        if(isHttp)
            port = 80;

        HttpRequest req = createRequest(host);

        log.info("starting socket");
        HttpClient client = IntegGoogleHttps.createHttpClient(isHttp);

        HttpClientSocket socket = client.openHttpSocket("oneTimer");
        socket
                .connect(new InetSocketAddress(host, port))
                .thenApply(s -> {
                    for(int i = 0; i < 1000; i+=100) {
                    	executor.execute(new WorkItem(s, req, i, i));
                    }
                	return s;
                })
                .exceptionally(e -> {
                    reportException(e);
                    return null;
                });

        Thread.sleep(10000000);
    }

    private static Void reportException(Throwable e) {
        log.error("exception on socket", e);
        return null;
    }

    private static class ChunkedResponseListener implements ResponseListener {

        private int id;

		public ChunkedResponseListener(int id) {
        	this.id = id;
		}

		@Override
        public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId id, boolean isComplete) {
            log.info("received req="+req+"resp="+resp+" id=" + id +" iscomplete="+isComplete);
        }

        @Override
        public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isLastData) {
            log.info("received resp="+ data +" id=" + id + " last="+ isLastData);
            if(isLastData) {
            	if(id.getValue() % 2 == 1) {
            		synchronized (completed) {
            			completed.add(this.id);
            		}
            	} else {
            		synchronized (completedPush) {
            			completedPush.add(this.id);
					}
            	}
            }

            log.info("completed="+completed.size()+" pushcomplete="+completedPush.size()+" sent="+sent.size()+" completed="+completed);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void incomingTrailer(List<Http2Header> headers, ResponseId id, boolean isComplete) {
            log.info("received trailer" + headers +" id=" + id + " last="+ isComplete);
        }

        @Override
        public void failure(Throwable e) {
            log.error("failed", e);
        }

    }


    private static HttpRequest createRequest(String host) {
        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setMethod(KnownHttpMethod.GET);
        requestLine.setUri(new HttpUri("/"));

        HttpRequest req = new HttpRequest();
        req.setRequestLine(requestLine);
        req.addHeader(new Header(KnownHeaderName.HOST, host));
        req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
        req.addHeader(new Header(KnownHeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        req.addHeader(new Header(KnownHeaderName.USER_AGENT, "nghttp2/1.15.0"));
        return req;
    }
}

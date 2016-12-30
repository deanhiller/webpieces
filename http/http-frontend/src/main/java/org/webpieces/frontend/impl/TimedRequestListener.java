package org.webpieces.frontend.impl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.HttpServerSocket;
import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.exceptions.HttpClientException;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.SafeRunnable;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

class TimedRequestListener implements RequestListener {

	private static final Logger log = LoggerFactory.getLogger(TimedRequestListener.class);

	private ScheduledExecutorService timer;
	private RequestListener listener;
	private FrontendConfig config;
	private Map<HttpSocket, ScheduledFuture<?>> socketToTimeout = new Hashtable<>();

	TimedRequestListener(ScheduledExecutorService timer, RequestListener listener, FrontendConfig config) {
		this.timer = timer;
		this.listener = listener;
		this.config = config;
	}

    private HttpServerSocket getHttpServerSocketForChannel(Channel channel) {
        ChannelSession session = channel.getSession();
        return (HttpServerSocket) session.get("webpieces.httpServerSocket");
    }

	@Override
    public void incomingRequest(HttpRequest req, RequestId id, boolean isComplete, ResponseSender responseSender) {
		releaseTimeout(getHttpServerSocketForChannel(responseSender.getUnderlyingChannel()));
		listener.incomingRequest(req, id, isComplete, responseSender);
	}

	private void releaseTimeout(HttpSocket httpSocket) {
		ScheduledFuture<?> scheduledFuture = socketToTimeout.remove(httpSocket);
		if(scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
	}

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
        return listener.incomingData(data, id, isComplete, sender);
    }

	@Override
	public void incomingTrailer(List<Http2Header> headers, RequestId id, boolean isComplete, ResponseSender sender) {
		listener.incomingTrailer(headers, id, isComplete, sender);
	}

	@Override
    public void clientOpenChannel(HttpSocket HttpSocket) {
        listener.clientOpenChannel(HttpSocket);
    }

    @Override
    public void incomingError(HttpException exc, HttpSocket httpSocket) {
		listener.incomingError(exc, httpSocket);
		
		//safety measure preventing leak on quick connect/close clients
		releaseTimeout(httpSocket);
		
		log.info("closing socket="+httpSocket+" due to response code="+exc.getStatusCode());
        ((HttpServerSocket) httpSocket).getResponseSender().close();
		listener.channelClosed(httpSocket, false);
	}

	void openedConnection(HttpServerSocket httpServerSocket, boolean isReadyForWrites) {
        log.info("opened connection from " + httpServerSocket + " isReadyForWrites=" + isReadyForWrites);
		if(!httpServerSocket.getUnderlyingChannel().isSslChannel()) {
			scheduleTimeout(httpServerSocket);
            clientOpenChannel(httpServerSocket);
		} else if(isReadyForWrites) {
			//if ready for writes, the tcpChannel is encrypted and fully open
			clientOpenChannel(httpServerSocket);
		} else { //if not ready for writes, the socket is open but encryption handshake is not been done yet
			scheduleTimeout(httpServerSocket);
		}
	}

	private void scheduleTimeout(HttpSocket HttpSocket) {
		if(timer == null || config.maxConnectToRequestTimeoutMs == null)
			return;
		
		ScheduledFuture<?> future = timer.schedule(new TimeoutOnRequest(HttpSocket), config.maxConnectToRequestTimeoutMs, TimeUnit.MILLISECONDS);
		//lifecycle of the entry in the Map is until the TimeoutOnRequest runs OR
		//until incomingRequest is invoked as we have a request OR
		//client closes the socket before sending http request and before the timeout
		socketToTimeout.put(HttpSocket, future);
	}

	private class TimeoutOnRequest extends SafeRunnable {
		
		private HttpSocket httpSocket;

		TimeoutOnRequest(HttpSocket httpSocket) {
			this.httpSocket = httpSocket;
		}

		@Override
		public void runImpl() {
			socketToTimeout.remove(httpSocket);
			log.info("timing out a client that did not send a request in time="+config.maxConnectToRequestTimeoutMs+"ms so we are closing that client's socket. httpSocket="+ httpSocket);
			
			HttpClientException exc = new HttpClientException("timing out a client who did not send a request in time", KnownStatusCode.HTTP_408_REQUEST_TIMEOUT);
			incomingError(exc, httpSocket);
		}
	}
	
	@Override
    public void channelClosed(HttpSocket httpSocket, boolean browserClosed) {
		releaseTimeout(httpSocket);
		listener.channelClosed(httpSocket, browserClosed);
	}

	@Override
    public void applyWriteBackPressure(ResponseSender responseSender) {
		listener.applyWriteBackPressure(responseSender);
	}

	@Override
    public void releaseBackPressure(ResponseSender responseSender) {
		listener.releaseBackPressure(responseSender);
	}

}

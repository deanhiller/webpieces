package org.webpieces.frontend.impl;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.httpcommon.api.RequestListener;
import org.webpieces.httpcommon.api.exceptions.HttpClientException;
import org.webpieces.httpcommon.api.exceptions.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.threading.SafeRunnable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

class TimedListener implements RequestListener {

	private static final Logger log = LoggerFactory.getLogger(TimedListener.class);

	private ScheduledExecutorService timer;
	private RequestListener listener;
	private FrontendConfig config;
	private Map<ResponseSender, ScheduledFuture<?>> socketToTimeout = new Hashtable<>();

	TimedListener(ScheduledExecutorService timer, RequestListener listener, FrontendConfig config) {
		this.timer = timer;
		this.listener = listener;
		this.config = config;
	}


	@Override
    public void incomingRequest(HttpRequest req, RequestId id, boolean isComplete, ResponseSender responseSender) {
		releaseTimeout(responseSender);
		listener.incomingRequest(req, id, isComplete, responseSender);
	}

	private void releaseTimeout(ResponseSender responseSender) {
		ScheduledFuture<?> scheduledFuture = socketToTimeout.remove(responseSender);
		if(scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
	}

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
        return listener.incomingData(data, id, isComplete, sender);
    }

    @Override
    public void clientOpenChannel(ResponseSender responseSender) {
        // This is bypassed by openedConnection
        throw new NotImplementedException();
    }

    @Override
    public void incomingError(HttpException exc, ResponseSender responseSender) {
		listener.incomingError(exc, responseSender);
		
		//safety measure preventing leak on quick connect/closeSocket clients
		releaseTimeout(responseSender);
		
		log.info("closing channel="+responseSender+" due to response code="+exc.getStatusCode());
		responseSender.close();
		listener.clientClosedChannel(responseSender);
	}

	void openedConnection(ResponseSender responseSender, boolean isReadyForWrites) {
		if(!responseSender.getUnderlyingChannel().isSslChannel()) {
			scheduleTimeout(responseSender);
			listener.clientOpenChannel(responseSender);
		} else if(isReadyForWrites) {
			//if ready for writes, the channel is encrypted and fully open
			listener.clientOpenChannel(responseSender);
		} else { //if not ready for writes, the socket is open but encryption handshake is not been done yet
			scheduleTimeout(responseSender);
		}
	}

	private void scheduleTimeout(ResponseSender responseSender) {
		if(timer == null || config.maxConnectToRequestTimeoutMs == null)
			return;
		
		ScheduledFuture<?> future = timer.schedule(new TimeoutOnRequest(responseSender), config.maxConnectToRequestTimeoutMs, TimeUnit.MILLISECONDS);
		//lifecycle of the entry in the Map is until the TimeoutOnRequest runs OR
		//until incomingRequest is invoked as we have a request OR
		//client closes the socket before sending http request and before the timeout
		socketToTimeout.put(responseSender, future);
	}

	private class TimeoutOnRequest extends SafeRunnable {
		
		private ResponseSender channel;

		TimeoutOnRequest(ResponseSender channel) {
			this.channel = channel;
		}

		@Override
		public void runImpl() {
			socketToTimeout.remove(channel);
			log.info("timing out a client that did not send a request in time="+config.maxConnectToRequestTimeoutMs+"ms so we are closing that client's socket. channel="+channel);
			
			HttpClientException exc = new HttpClientException("timing out a client who did not send a request in time", KnownStatusCode.HTTP_408_REQUEST_TIMEOUT);
			incomingError(exc, channel);
		}
	}
	
	public void clientClosedChannel(ResponseSender responseSender) {
		listener.clientClosedChannel(responseSender);
	}

	public void applyWriteBackPressure(ResponseSender responseSender) {
		listener.applyWriteBackPressure(responseSender);
	}

	public void releaseBackPressure(ResponseSender responseSender) {
		listener.releaseBackPressure(responseSender);
	}

	


}

package org.webpieces.frontend.impl;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.frontend.api.FrontendConfig;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.frontend.api.HttpRequestListener;
import org.webpieces.frontend.api.exception.HttpClientException;
import org.webpieces.frontend.api.exception.HttpException;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.util.threading.SafeRunnable;

public class TimedListener {

	private static final Logger log = LoggerFactory.getLogger(TimedListener.class);

	private ScheduledExecutorService timer;
	private HttpRequestListener listener;
	private FrontendConfig config;
	private Map<FrontendSocket, ScheduledFuture<?>> socketToTimeout = new Hashtable<>();

	public TimedListener(ScheduledExecutorService timer, HttpRequestListener listener, FrontendConfig config) {
		this.timer = timer;
		this.listener = listener;
		this.config = config;
	}

	public void processHttpRequests(FrontendSocket channel, HttpRequest req, boolean isHttps) {
		releaseTimeout(channel);
		listener.processHttpRequests(channel, req, isHttps);
	}

	private void releaseTimeout(FrontendSocket channel) {
		ScheduledFuture<?> scheduledFuture = socketToTimeout.remove(channel);
		if(scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
	}

	public void sendServerResponse(FrontendSocket channel, HttpException exc, KnownStatusCode status) {
		listener.sendServerResponse(channel, exc, status);
		
		//safety measure preventing leak on quick connect/close clients
		releaseTimeout(channel);
		
		log.info("closing channel="+channel+" due to response code="+status);
		channel.close();
		listener.clientClosedChannel(channel);
	}

	public void clientOpenChannel(FrontendSocket channel, boolean isReadyForWrites) {
		if(!channel.getUnderlyingChannel().isSslChannel()) {
			scheduleTimeout(channel);
			listener.clientOpenChannel(channel);
		} else if(isReadyForWrites) {
			//if ready for writes, the channel is encrypted and fully open
			listener.clientOpenChannel(channel);
		} else { //if not ready for writes, the socket is open but encryption handshake is not been done yet
			scheduleTimeout(channel);
		}
	}

	private void scheduleTimeout(FrontendSocket channel) {
		if(timer == null)
			return;
		
		ScheduledFuture<?> future = timer.schedule(new TimeoutOnRequest(channel), config.maxConnectToRequestTimeoutMs, TimeUnit.MILLISECONDS);
		//lifecycle of the entry in the Map is until the TimeoutOnRequest runs OR
		//until processHttpRequests is invoked as we have a request OR
		//client closes the socket before sending http request and before the timeout
		socketToTimeout.put(channel, future);
	}

	private class TimeoutOnRequest extends SafeRunnable {
		
		private FrontendSocket channel;

		public TimeoutOnRequest(FrontendSocket channel) {
			this.channel = channel;
		}

		@Override
		public void runImpl() {
			socketToTimeout.remove(channel);
			log.info("timing out a client that did not send a request in time="+config.maxConnectToRequestTimeoutMs+"ms so we are closing that client's socket");
			
			HttpClientException exc = new HttpClientException("timing out a client who did not send a request in time");
			sendServerResponse(channel, exc, KnownStatusCode.HTTP408);
		}
	}
	
	public void clientClosedChannel(FrontendSocket channel) {
		listener.clientClosedChannel(channel);
	}

	public void applyWriteBackPressure(FrontendSocket channel) {
		listener.applyWriteBackPressure(channel);
	}

	public void releaseBackPressure(FrontendSocket channel) {
		listener.releaseBackPressure(channel);
	}

	


}

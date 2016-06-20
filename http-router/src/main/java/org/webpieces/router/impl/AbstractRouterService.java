package org.webpieces.router.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.ResponseStreamer;
import org.webpieces.router.api.RoutingService;
import org.webpieces.router.api.dto.RedirectResponse;
import org.webpieces.router.api.dto.RenderResponse;
import org.webpieces.router.api.dto.RouterRequest;

public abstract class AbstractRouterService implements RoutingService {
	
	protected boolean started = false;
	
	@Override
	public final void processHttpRequests(RouterRequest req, ResponseStreamer responseCb) {
		LoggingStreamer str = new LoggingStreamer(responseCb);
		try {
			if(!started)
				throw new IllegalStateException("Either start was not called by client or start threw an exception that client ignored and must be fixed");;
			
			processHttpRequestsImpl(req, str);
		} catch (Throwable e) {
			str.failure(e);
		}
	}

	protected abstract void processHttpRequestsImpl(RouterRequest req, ResponseStreamer responseCb);
	
	private static class LoggingStreamer implements ResponseStreamer {
		private static final Logger log = LoggerFactory.getLogger(LoggingStreamer.class);
		private ResponseStreamer responseCb;

		public void sendRedirect(RedirectResponse httpResponse) {
			responseCb.sendRedirect(httpResponse);
		}

		public void sendRenderHtml(RenderResponse resp) {
			responseCb.sendRenderHtml(resp);
		}

		public void failure(Throwable e) {
			log.warn("Exception", e);
			responseCb.failure(e);
		}

		public LoggingStreamer(ResponseStreamer responseCb) {
			this.responseCb = responseCb;
		}

	}
}

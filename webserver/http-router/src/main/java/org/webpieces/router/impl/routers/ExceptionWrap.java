package org.webpieces.router.impl.routers;

import org.webpieces.router.api.exceptions.WebSocketClosedException;

public class ExceptionWrap {

	public static boolean isChannelClosed(Throwable e) {
		Throwable cause = e;
		while(cause.getCause() != null) {
			if(cause instanceof WebSocketClosedException)
				return true;
			cause = cause.getCause();
		}
		
		return false;
	}

	
}

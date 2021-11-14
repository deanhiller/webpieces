package org.webpieces.httpclient11.api;

import org.webpieces.util.futures.XFuture;

public interface HttpStreamRef {
	
	public XFuture<HttpDataWriter> getWriter();
	
	public XFuture<Void> cancel(Object reason);

}

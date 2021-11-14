package org.webpieces.util.futures;

import org.webpieces.util.futures.XFuture;

public interface Session {

	void setProcessFuturee(XFuture<Void> future);

	XFuture<Void> getProcessFuture();

}

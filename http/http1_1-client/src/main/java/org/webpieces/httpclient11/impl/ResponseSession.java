package org.webpieces.httpclient11.impl;

import org.webpieces.util.futures.XFuture;

public class ResponseSession {

	private XFuture<Void> processFuture = XFuture.completedFuture(null);

	public XFuture<Void> getProcessFuture() {
		return processFuture;
	}

	public void setProcessFuture(XFuture<Void> future) {
		this.processFuture = future;
	}

}

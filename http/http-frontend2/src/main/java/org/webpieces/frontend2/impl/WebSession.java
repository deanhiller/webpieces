package org.webpieces.frontend2.impl;

import org.webpieces.util.futures.XFuture;

public class WebSession {

	//start out completed
	private XFuture<Void> processFuture = XFuture.completedFuture(null);

	public XFuture<Void> getProcessFuture() {
		return processFuture;
	}

	public void setProcessFuture(XFuture<Void> processFuture) {
		this.processFuture = processFuture;
	}

}

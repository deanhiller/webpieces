package org.webpieces.httpclient11.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.handlers.DataListener;

public interface ChannelProxy {

	XFuture<Void> connect(HostWithPort addr, DataListener dataListener);

	/**
	 * @deprecated
	 */
	@Deprecated
	XFuture<Void> connect(InetSocketAddress addr, DataListener dataListener);

	XFuture<Void> write(ByteBuffer wrap);

	XFuture<Void> close();

	String getId();

	boolean isSecure();

}

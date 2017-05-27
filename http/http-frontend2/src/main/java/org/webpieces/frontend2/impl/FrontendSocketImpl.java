package org.webpieces.frontend2.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.frontend2.api.StreamListener;
import org.webpieces.frontend2.api.ServerSocketInfo;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.nio.api.channels.ChannelSession;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2engine.api.error.FarEndClosedConnection;
import com.webpieces.http2engine.api.error.ShutdownStream;
import com.webpieces.http2engine.api.server.Http2ServerEngine;

public class FrontendSocketImpl implements FrontendSocket {

	private static final Logger log = LoggerFactory.getLogger(FrontendSocketImpl.class);
	
	private TCPChannel channel;
	private ProtocolType protocol;
	private Memento http1_1ParseState;
	private Http2ServerEngine http2Engine;
	private StreamWriter writer;
	private ConcurrentLinkedQueue<Http1_1StreamImpl> http11Queue = new ConcurrentLinkedQueue<>();
	private boolean isClosed;
	private ServerSocketInfo svrSocketInfo;

	public FrontendSocketImpl(TCPChannel channel, ProtocolType protocol, ServerSocketInfo svrSocketInfo) {
		this.channel = channel;
		this.protocol = protocol;
		this.svrSocketInfo = svrSocketInfo;
	}

	public ProtocolType getProtocol() {
		return protocol;
	}

	@Override
	public void close(String reason) {
		//need to do goAway here
		internalClose();
	}

	public void internalClose() {
		channel.close();
	}
	
	public void setHttp1_1ParseState(Memento parseState) {
		this.http1_1ParseState = parseState;
	}

	public Memento getHttp1_1ParseState() {
		return http1_1ParseState;
	}

	public void setProtocol(ProtocolType protocol) {
		this.protocol = protocol;
	}

	public void setHttp2Engine(Http2ServerEngine engine) {
		this.http2Engine = engine;
	}

	public Http2ServerEngine getHttp2Engine() {
		return http2Engine;
	}

	public TCPChannel getChannel() {
		return channel;
	}

	public void addWriter(StreamWriter writer) {
		this.writer = writer;
	}

	public StreamWriter getWriter() {
		return writer;
	}

	public void setAddStream(Http1_1StreamImpl stream) {
		http11Queue.add(stream);
	}

	public void removeStream(Http1_1StreamImpl http1_1StreamImpl) {
		if(isClosed)
			throw new IllegalStateException("The socket is closed(the far end most likely closed it)");
		
		Http1_1StreamImpl str = http11Queue.poll();
		if(str != http1_1StreamImpl)
			throw new IllegalArgumentException("bug, these streams should always match");
	}

	public Http1_1StreamImpl getCurrentStream() {
		if(isClosed)
			throw new IllegalStateException("The socket is closed(the far end most likely closed it)");
		return http11Queue.peek();
	}

	public void farEndClosed(StreamListener httpListener) {
		isClosed = true;
		FarEndClosedConnection conn = new FarEndClosedConnection(this+" The far end closed the socket");
		if(protocol == ProtocolType.HTTP1_1) {
			cancelAllStreams(httpListener, conn);
		}
		
	}

	private void cancelAllStreams(StreamListener httpListener, FarEndClosedConnection f) {
		while(true) {
			Http1_1StreamImpl poll = http11Queue.poll();
			if(poll == null)
				break;
			
			ShutdownStream shutdown = new ShutdownStream(poll.getStreamId(), f);
			fireCancel(shutdown, poll);
		}
	}

	private void fireCancel(ShutdownStream f, Http1_1StreamImpl stream) {
		try {
			stream.getStreamHandle().cancel(f);
		} catch(Throwable e) {
			log.warn("exception from stream trying to cancel.  swallowing and continuing", e);
		}
	}

	@Override
	public ChannelSession getSession() {
		return channel.getSession();
	}

	@Override
	public String toString() {
		return "HttpSocket[" + channel+"]";
	}

	@Override
	public boolean isHttps() {
		return svrSocketInfo.isHttps();
	}

	@Override
	public InetSocketAddress getServerLocalBoundAddress() {
		return svrSocketInfo.getLocalBoundAddress();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}
}

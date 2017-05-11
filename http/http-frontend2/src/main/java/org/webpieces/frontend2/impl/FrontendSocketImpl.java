package org.webpieces.frontend2.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.frontend2.api.FrontendSocket;
import org.webpieces.httpparser.api.Memento;
import org.webpieces.nio.api.channels.TCPChannel;

import com.webpieces.http2engine.api.server.Http2ServerEngine;

public class FrontendSocketImpl implements FrontendSocket {

	private TCPChannel channel;
	private ProtocolType protocol;
	private Memento http1_1ParseState;
	private Http2ServerEngine http2Engine;

	public FrontendSocketImpl(TCPChannel channel, ProtocolType protocol) {
		this.channel = channel;
		this.protocol = protocol;
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

	public CompletableFuture<FrontendSocket> write(ByteBuffer buf) {
		return channel.write(buf).thenApply(c -> this);
	}

}

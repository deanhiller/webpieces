package com.webpieces.hpack.impl;

import java.io.IOException;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.MarshalState;
import org.webpieces.util.SneakyThrow;

public class MarshalStateImpl implements MarshalState {

	private HeaderEncoding encoding;
	private Encoder encoder;
	private volatile long remoteMaxFrameSize;

	public MarshalStateImpl(HeaderEncoding encoding, Encoder encoder, long remoteMaxFrameSize) {
		this.encoding = encoding;
		this.encoder = encoder;
		this.remoteMaxFrameSize = remoteMaxFrameSize;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	public long getMaxRemoteFrameSize() {
		return remoteMaxFrameSize;
	}

	@Override
	public void setOutoingMaxFrameSize(long maxFrameSize) {
		remoteMaxFrameSize = maxFrameSize;
	}
	
	@Override
    public void setOutgoingMaxTableSize(int newSize) {
		try {
			encoding.setMaxHeaderTableSize(encoder, newSize);
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
    }

}

package com.webpieces.hpack.impl;

import com.twitter.hpack.Encoder;
import com.webpieces.hpack.api.MarshalState;

public class MarshalStateImpl implements MarshalState {

	private Encoder encoder;

	public MarshalStateImpl(Encoder encoder) {
		this.encoder = encoder;
	}

	public Encoder getEncoder() {
		return encoder;
	}

}

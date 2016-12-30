package com.webpieces.http2engine.impl;

import org.webpieces.javasm.api.Memento;

public class Stream {

	private Memento currentState;

	public Stream(Memento currentState) {
		this.currentState = currentState;
	}

	public Memento getCurrentState() {
		return currentState;
	}

}

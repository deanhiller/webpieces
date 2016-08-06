package org.webpieces.templating.impl.source;

import org.webpieces.templating.api.GroovyGen;

public class TagState {

	private TokenImpl token;
	private GroovyGen generator;

	public TagState(TokenImpl token, GroovyGen generator) {
		this.token = token;
		this.generator = generator;
	}

	public TokenImpl getToken() {
		return token;
	}

	public GroovyGen getGenerator() {
		return generator;
	}

}

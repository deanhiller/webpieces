package org.webpieces.templating.impl.source;

import org.webpieces.templating.api.GroovyGen;

public class TagState {

	private TokenImpl token;
	private GroovyGen generator;
	private int uniqueId;

	public TagState(TokenImpl token, GroovyGen generator, int uniqueId) {
		this.token = token;
		this.generator = generator;
		this.uniqueId = uniqueId;
	}

	public TokenImpl getToken() {
		return token;
	}

	public GroovyGen getGenerator() {
		return generator;
	}

	public int getUniqueId() {
		return uniqueId;
	}

}

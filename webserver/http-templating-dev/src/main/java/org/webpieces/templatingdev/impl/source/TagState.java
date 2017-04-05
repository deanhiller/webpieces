package org.webpieces.templatingdev.impl.source;

import org.webpieces.templatingdev.api.HtmlGen;

public class TagState {

	private TokenImpl token;
	private HtmlGen generator;
	private int uniqueId;

	public TagState(TokenImpl token, HtmlGen generator, int uniqueId) {
		this.token = token;
		this.generator = generator;
		this.uniqueId = uniqueId;
	}

	public TokenImpl getToken() {
		return token;
	}

	public HtmlGen getGenerator() {
		return generator;
	}

	public int getUniqueId() {
		return uniqueId;
	}

}

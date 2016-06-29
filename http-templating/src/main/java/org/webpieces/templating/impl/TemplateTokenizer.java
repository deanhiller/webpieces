package org.webpieces.templating.impl;

import java.util.List;

import org.webpieces.templating.impl.source.Token;
import org.webpieces.templating.impl.source.TokenizeHelper;

public class TemplateTokenizer {

	public List<Token> tokenize(String source) {
		return new TokenizeHelper(source).parseSource();
	}
	
}
